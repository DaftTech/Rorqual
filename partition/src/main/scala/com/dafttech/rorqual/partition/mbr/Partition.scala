package com.dafttech.rorqual.partition.mbr

import com.dafttech.rorqual.partition.{ParseError, ParseResult, Severity}
import com.dafttech.rorqual.partition.ParseResult.*
import cats.data.{Ior, NonEmptyList}
import scodec.bits.{ByteOrdering, ByteVector}

/** A single MBR partition table entry (16 bytes).
  *
  * The `status` byte uses the convention:
  *   - `0x80` — active/bootable
  *   - `0x00` — inactive
  *   - anything else — invalid (recorded as a warning, value preserved)
  *
  * Empty/unused entries have `partitionType == 0` and are represented by the parser
  * returning `None`.
  */
final case class Partition(
    status: Int,
    startAddress: CHS,
    partitionType: Int,
    endAddress: CHS,
    startSector: Long,
    sectors: Long
):
  def bootable: Boolean = status == 0x80
  def active: Boolean   = status == 0x00
  def statusValid: Boolean = bootable || active

  def toBytes: ByteVector =
    ByteVector.fromInt(status & 0xff, size = 1) ++
      startAddress.toBytes ++
      ByteVector.fromInt(partitionType & 0xff, size = 1) ++
      endAddress.toBytes ++
      ByteVector.fromLong(startSector, size = 4, ordering = ByteOrdering.LittleEndian) ++
      ByteVector.fromLong(sectors,     size = 4, ordering = ByteOrdering.LittleEndian)

object Partition:

  val EntrySize: Int = 16

  /** Parse one 16-byte entry at `baseOffset`.
    *
    * Returns:
    *   - `Ior.Right(None)`             — empty slot (partitionType == 0).
    *   - `Ior.Right(Some(p))`          — clean entry.
    *   - `Ior.Both(errs, Some(p))`     — entry with validation warnings.
    *   - `Ior.Left(errs)`              — entry unparseable (e.g. truncated).
    */
  def parse(bytes: ByteVector, baseOffset: Long, fieldPath: String): ParseResult[Option[Partition]] =
    if bytes.size < EntrySize then
      ParseResult.failure(
        ParseError(
          baseOffset, fieldPath,
          s"truncated partition entry: need $EntrySize bytes, got ${bytes.size}",
          Severity.Fatal
        )
      )
    else
      val status        = bytes(0) & 0xff
      val partitionType = bytes(4) & 0xff
      val startSector =
        bytes.slice(8, 12).toLong(signed = false, ordering = ByteOrdering.LittleEndian)
      val sectors =
        bytes.slice(12, 16).toLong(signed = false, ordering = ByteOrdering.LittleEndian)

      // Empty slot: short-circuit (matches mkfs/fdisk convention).
      if partitionType == 0 then
        ParseResult.success(None)
      else
        val startChsRes = CHS.parse(bytes.slice(1, 4), baseOffset + 1, s"$fieldPath.startCHS")
        val endChsRes   = CHS.parse(bytes.slice(5, 8), baseOffset + 5, s"$fieldPath.endCHS")

        val statusErr =
          if status == 0x80 || status == 0x00 then None
          else
            Some(ParseError(
              baseOffset, s"$fieldPath.status",
              f"unexpected status byte 0x$status%02x (expected 0x00 or 0x80)",
              Severity.Warning
            ))

        // Combine the two CHS results plus the status warning. We always have
        // *some* recovered value (status + LBA fields are always parseable here),
        // so we use Ior.Both for any accumulated errors.
        val chsErrs: List[ParseError] =
          startChsRes.errorList ++ endChsRes.errorList ++ statusErr.toList
        val startChs = startChsRes.recovered.getOrElse(CHS(0, 0, 0))
        val endChs   = endChsRes.recovered.getOrElse(CHS(0, 0, 0))

        val partition = Partition(
          status        = status,
          startAddress  = startChs,
          partitionType = partitionType,
          endAddress    = endChs,
          startSector   = startSector,
          sectors       = sectors
        )

        chsErrs match
          case Nil          => ParseResult.success(Some(partition))
          case head :: tail => Ior.Both(NonEmptyList(head, tail), Some(partition))
