package com.dafttech.rorqual.partition.mbr

import com.dafttech.rorqual.partition.Severity
import com.dafttech.rorqual.partition.ParseResult.*
import munit.FunSuite
import scodec.bits.{ByteVector, hex}

class MBRSuite extends FunSuite:

  // A minimal, well-formed MBR with one Linux partition (type 0x83) at LBA 2048, 100 sectors.
  private val cleanMbr: MBR =
    val p = Partition(
      status = 0x80,
      startAddress = CHS.fromLba(2048),
      partitionType = 0x83,
      endAddress = CHS.fromLba(2048 + 100 - 1),
      startSector = 2048,
      sectors = 100
    )
    MBR(
      bootstrap  = ByteVector.fill(MBR.BootstrapSize.toLong)(0),
      partitions = List(Some(p), None, None, None)
    )

  test("round-trip toBytes/parse produces equal MBR with no errors") {
    val bytes  = cleanMbr.toBytes
    assertEquals(bytes.size, MBR.Size.toLong)
    val parsed = MBR.parse(bytes)
    assertEquals(parsed.hasErrors, false)
    assertEquals(parsed.recovered, Some(cleanMbr))
  }

  test("missing 0x55AA signature → warning, parsing continues") {
    val damaged = cleanMbr.toBytes.dropRight(2) ++ hex"0000"
    val parsed  = MBR.parse(damaged)
    assert(parsed.recovered.isDefined, "MBR should still be recovered")
    val errs = parsed.errorList
    val sigErr = errs.find(_.field == "mbr.signature")
    assert(sigErr.isDefined, s"expected mbr.signature error, got $errs")
    assertEquals(sigErr.get.offset, MBR.SignatureOffset.toLong)
    assertEquals(sigErr.get.severity, Severity.Warning)
    // structural content is preserved
    assertEquals(parsed.recovered.get.partitions.flatten.size, 1)
  }

  test("corrupt CHS in one partition does not blow up the others") {
    // Mutate startCHS of entry 0 to an invalid encoding (head=255, sector=0).
    // (Cylinder can't be encoded out-of-range — the 10-bit field truncates — so
    // we damage the head and sector fields instead.)
    val bytes = cleanMbr.toBytes.toArray
    // entry 0 starts at offset 446; startCHS = bytes [447..449]
    val cylinder = 100
    val head     = 255          // > 254 → error
    val sector   = 0            // < 1   → error
    bytes(447)   = head.toByte
    bytes(448)   = ((((cylinder >>> 8) & 0x03) << 6) | (sector & 0x3f)).toByte
    bytes(449)   = (cylinder & 0xff).toByte
    // Add a *second* partition entry (slot 1) so we can verify it survives.
    val p2Bytes = Partition(
      status = 0x00,
      startAddress = CHS.fromLba(5000),
      partitionType = 0x07,
      endAddress = CHS.fromLba(5099),
      startSector = 5000,
      sectors = 100
    ).toBytes.toArray
    System.arraycopy(p2Bytes, 0, bytes, 446 + 16, 16)

    val parsed = MBR.parse(ByteVector.view(bytes))

    val errs = parsed.errorList
    assert(errs.exists(_.field.startsWith("mbr.partitions[0].startCHS")),
           s"expected error on partition[0].startCHS, got $errs")
    // Entry 1 must be recovered intact
    val rec = parsed.recovered.get
    assertEquals(rec.partitions(1).map(_.partitionType), Some(0x07))
    assertEquals(rec.partitions(1).map(_.sectors),       Some(100L))
    // Entry 0 is still recovered, just with diagnostics attached
    assertEquals(rec.partitions(0).map(_.partitionType), Some(0x83))
  }

  test("invalid status byte → warning, entry still recovered") {
    val bytes = cleanMbr.toBytes.toArray
    bytes(446) = 0x42.toByte  // not 0x00 or 0x80
    val parsed = MBR.parse(ByteVector.view(bytes))
    val errs = parsed.errorList
    val statusErr = errs.find(_.field == "mbr.partitions[0].status")
    assert(statusErr.isDefined, s"expected status warning, got $errs")
    assertEquals(statusErr.get.severity, Severity.Warning)
    assertEquals(parsed.recovered.get.partitions(0).map(_.status), Some(0x42))
  }

  test("truncated MBR (< 512 bytes) is parsed with a truncation warning") {
    val parsed = MBR.parse(ByteVector.fill(256L)(0))
    val errs = parsed.errorList
    assert(errs.exists(_.field == "mbr"), s"expected truncation error in $errs")
    // truncated + no signature → at least one entry of each
    assert(parsed.recovered.isDefined)
  }

  test("empty partition slots round-trip as None") {
    val empty = MBR(
      bootstrap  = ByteVector.fill(MBR.BootstrapSize.toLong)(0),
      partitions = List.fill(4)(None)
    )
    val parsed = MBR.parse(empty.toBytes)
    assertEquals(parsed.hasErrors, false)
    assertEquals(parsed.recovered.get.partitions, List.fill(4)(None))
  }

  test("CHS.parse records zero-CHS as valid (sentinel)") {
    val r = CHS.parse(ByteVector(0, 0, 0), 0, "test")
    assertEquals(r.hasErrors, false)
    assertEquals(r.recovered, Some(CHS(0, 0, 0)))
  }

  test("CHS.parse accumulates multiple validation errors") {
    // head=255 (>254), sector=0 (<1) — two simultaneous errors.
    // Cylinder can't be encoded out-of-range (10-bit field), so it's not in this set.
    val bytes = ByteVector(0xff.toByte, 0x00.toByte, 0x10.toByte)
    val r = CHS.parse(bytes, baseOffset = 100, fieldPath = "x")
    val errs = r.errorList
    assertEquals(errs.size, 2, errs.toString)
    assert(errs.forall(_.offset == 100L))
    assert(errs.forall(_.field.startsWith("x.")))
    assert(errs.exists(_.field == "x.head"))
    assert(errs.exists(_.field == "x.sector"))
  }
