package com.dafttech.rorqual.partition.mbr

import com.dafttech.rorqual.partition.{ParseError, ParseResult, Severity}
import com.dafttech.rorqual.partition.ParseResult.*
import cats.data.{Ior, NonEmptyList}
import scodec.bits.{ByteVector, hex}

/** A parsed Master Boot Record.
  *
  * The four partition entries are preserved as a list of `Option[Partition]`, with
  * `None` for empty slots. Empty slots being explicit (rather than dropped) matters
  * for forensic round-tripping — `toBytes` must place each entry at its original
  * offset.
  */
final case class MBR(
    bootstrap: ByteVector,
    partitions: List[Option[Partition]]
):
  require(bootstrap.size == MBR.BootstrapSize, s"bootstrap must be ${MBR.BootstrapSize} bytes")
  require(partitions.size == MBR.PartitionCount, s"must have exactly ${MBR.PartitionCount} entries")

  /** Non-empty entries in slot order. */
  def nonEmpty: List[Partition] = partitions.flatten

  def toBytes: ByteVector =
    val entries = partitions
      .map(_.map(_.toBytes).getOrElse(ByteVector.fill(Partition.EntrySize.toLong)(0)))
    bootstrap ++ ByteVector.concat(entries) ++ MBR.Signature

object MBR:
  val Size: Int           = 512
  val BootstrapSize: Int  = 446
  val PartitionCount: Int = 4
  val TableOffset: Int    = BootstrapSize
  val TableSize: Int      = PartitionCount * Partition.EntrySize
  val SignatureOffset: Int = TableOffset + TableSize
  val Signature: ByteVector = hex"55AA"

  /** Soft-fail parse of a 512-byte sector.
    *
    * Behaviour:
    *   - Missing/wrong 0x55AA signature → warning, parsing continues.
    *   - Buffers shorter than 512 bytes are accepted; missing entries are zero-padded
    *     and a warning is recorded.
    *   - Each of the four partition entries is parsed independently; one bad entry
    *     never blocks the others.
    */
  def parse(bytes: ByteVector): ParseResult[MBR] =
    val truncationErrs: List[ParseError] =
      if bytes.size >= Size then Nil
      else
        List(
          ParseError(
            offset = bytes.size,
            field = "mbr",
            message = s"truncated MBR: need $Size bytes, got ${bytes.size}",
            severity = Severity.Warning
          )
        )

    val padded = if bytes.size < Size then bytes.padRight(Size.toLong) else bytes.take(Size.toLong)

    val signatureBytes = padded.slice(SignatureOffset.toLong, Size.toLong)
    val signatureErr: List[ParseError] =
      if signatureBytes == Signature then Nil
      else
        List(
          ParseError(
            offset = SignatureOffset.toLong,
            field = "mbr.signature",
            message = s"expected 55AA boot signature, got ${signatureBytes.toHex.toUpperCase}",
            severity = Severity.Warning
          )
        )

    val bootstrap = padded.slice(0, BootstrapSize.toLong)

    val entryResults: List[ParseResult[Option[Partition]]] =
      (0 until PartitionCount).toList.map { i =>
        val entryOffset = (TableOffset + i * Partition.EntrySize).toLong
        val entryBytes  = padded.slice(entryOffset, entryOffset + Partition.EntrySize)
        Partition.parse(entryBytes, entryOffset, s"mbr.partitions[$i]")
      }

    val entryErrs: List[ParseError] = entryResults.flatMap(_.errorList)
    val entries: List[Option[Partition]] =
      entryResults.map(_.recovered.flatten) // outer Option from `recovered`, inner from `Partition.parse`'s `Option`

    val mbr = MBR(bootstrap, entries)

    val allErrs = truncationErrs ++ signatureErr ++ entryErrs
    allErrs match
      case Nil          => ParseResult.success(mbr)
      case head :: tail => Ior.Both(NonEmptyList(head, tail), mbr)
