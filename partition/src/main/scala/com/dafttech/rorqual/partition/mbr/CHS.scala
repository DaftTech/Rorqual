package com.dafttech.rorqual.partition.mbr

import com.dafttech.rorqual.partition.{ParseError, ParseResult, Severity}
import cats.data.NonEmptyList
import cats.data.Ior
import scodec.bits.ByteVector

/** Cylinder/Head/Sector address as encoded in an MBR partition entry.
  *
  * Validation is *recorded*, never thrown — invalid CHS values are returned as-is
  * with diagnostics attached, so forensic tooling can see the raw bytes.
  */
final case class CHS(cylinder: Int, head: Int, sector: Int):

  /** "Empty" sentinel — all zeros, used by some tools to mark unused entries. */
  def isZero: Boolean = cylinder == 0 && head == 0 && sector == 0

  /** Convert to LBA using the standard 255-heads / 63-sectors-per-track geometry.
    *
    * This is only meaningful for non-zero, in-range CHS values. Caller should
    * check [[isZero]] and prefer the LBA fields in the partition entry when present.
    */
  def lba: Long =
    (cylinder.toLong * CHS.HeadsPerCylinder + head) * CHS.SectorsPerTrack + sector - 1L

  override def toString: String = s"CHS($cylinder, $head, $sector)"

  def toBytes: ByteVector =
    val head8     = ByteVector.fromInt(head & 0xff, size = 1)
    val sector6   = sector & 0x3f
    val cylinderH = (cylinder >>> 8) & 0x03
    val cylinderL = cylinder & 0xff
    val midByte   = ((cylinderH << 6) | sector6) & 0xff
    head8 ++ ByteVector(midByte.toByte, cylinderL.toByte)

object CHS:
  val HeadsPerCylinder: Int = 255
  val SectorsPerTrack: Int  = 63
  val Max: CHS              = CHS(cylinder = 1023, head = 254, sector = 63)

  /** Parse 3 bytes at `baseOffset` (used for error reporting).
    *
    * Returns the recovered CHS plus any validation diagnostics. Never throws.
    */
  def parse(bytes: ByteVector, baseOffset: Long, fieldPath: String): ParseResult[CHS] =
    if bytes.size < 3 then
      ParseResult.failure(
        ParseError(
          offset = baseOffset,
          field = fieldPath,
          message = s"truncated CHS: need 3 bytes, got ${bytes.size}",
          severity = Severity.Fatal
        )
      )
    else
      val b0   = bytes(0) & 0xff
      val b1   = bytes(1) & 0xff
      val b2   = bytes(2) & 0xff
      val head = b0
      val sector = b1 & 0x3f
      val cylinder = ((b1 & 0xc0) << 2) | b2

      val chs = CHS(cylinder = cylinder, head = head, sector = sector)

      // Validate independently and collect diagnostics. Zero CHS is fine (sentinel).
      if chs.isZero then ParseResult.success(chs)
      else
        val errs = List(
          Option.when(cylinder > 1023)(
            ParseError(baseOffset, s"$fieldPath.cylinder",
              s"cylinder $cylinder out of range [0, 1023]", Severity.Error)
          ),
          Option.when(head > 254)(
            ParseError(baseOffset, s"$fieldPath.head",
              s"head $head out of range [0, 254]", Severity.Error)
          ),
          Option.when(sector < 1 || sector > 63)(
            ParseError(baseOffset, s"$fieldPath.sector",
              s"sector $sector out of range [1, 63]", Severity.Error)
          )
        ).flatten

        errs match
          case Nil          => ParseResult.success(chs)
          case head :: tail => Ior.Both(NonEmptyList(head, tail), chs)

  /** Convert an LBA back to CHS. Returns [[Max]] if the value exceeds CHS range. */
  def fromLba(lba: Long): CHS =
    val cylinder = lba / (HeadsPerCylinder.toLong * SectorsPerTrack)
    val tmp      = lba % (HeadsPerCylinder.toLong * SectorsPerTrack)
    val head     = tmp / SectorsPerTrack
    val sector   = (tmp % SectorsPerTrack) + 1
    if cylinder > 1023 || head > 254 || sector > 63 then Max
    else CHS(cylinder.toInt, head.toInt, sector.toInt)
