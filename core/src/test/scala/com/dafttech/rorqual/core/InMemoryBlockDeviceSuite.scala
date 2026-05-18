package com.dafttech.rorqual.core

import cats.effect.IO
import munit.CatsEffectSuite
import scodec.bits.ByteVector

class InMemoryBlockDeviceSuite extends CatsEffectSuite:

  private def newDev(blocks: Long = 8, bs: Int = 512): IO[InMemoryBlockDevice[IO]] =
    InMemoryBlockDevice[IO]("test", blocks = blocks, logicalBlockSize = bs)

  test("reads from never-written blocks return zeros (sparse)") {
    newDev().flatMap { d =>
      d.openRead.use { r =>
        r.readBlocks(0, 2).map { bv =>
          assertEquals(bv.size, 1024L)
          assert(bv.toArray.forall(_ == 0.toByte))
        }
      }
    }
  }

  test("round-trip writeBlocks/readBlocks") {
    val payload = ByteVector.view(Array.tabulate[Byte](512)(i => i.toByte))
    newDev().flatMap { d =>
      d.openWrite.use { w =>
        for
          _   <- w.writeBlocks(3, payload)
          got <- w.readBlocks(3, 1)
          _   <- IO(assertEquals(got, payload))
          // adjacent block still sparse
          gap <- w.readBlocks(2, 1)
          _   <- IO(assert(gap.toArray.forall(_ == 0.toByte)))
        yield ()
      }
    }
  }

  test("writeBlocks rejects misaligned size") {
    newDev().flatMap { d =>
      d.openWrite.use { w =>
        w.writeBlocks(0, ByteVector.fill(513L)(1)).attempt.map { e =>
          assert(e.isLeft, s"expected failure, got $e")
        }
      }
    }
  }

  test("write out of bounds is rejected") {
    newDev(blocks = 4).flatMap { d =>
      d.openWrite.use { w =>
        w.writeBlocks(3, ByteVector.fill(1024L)(1)).attempt.map { e =>
          assert(e.isLeft)
        }
      }
    }
  }

  test("writeBytesAt performs RMW for partial blocks") {
    import fs2.Stream
    val payload = ByteVector(0x11, 0x22, 0x33, 0x44)
    newDev().flatMap { d =>
      d.openWrite.use { w =>
        for
          _   <- Stream.chunk(fs2.Chunk.byteVector(payload))
                   .through(w.writeBytesAt(510L))   // crosses block boundary
                   .compile
                   .drain
          // block 0: last 2 bytes should be 0x11, 0x22
          blk0 <- w.readBlocks(0, 1)
          _    <- IO(assertEquals(blk0.takeRight(2), ByteVector(0x11, 0x22)))
          // block 1: first 2 bytes should be 0x33, 0x44
          blk1 <- w.readBlocks(1, 1)
          _    <- IO(assertEquals(blk1.take(2), ByteVector(0x33, 0x44)))
        yield ()
      }
    }
  }

  test("readBytes streams bytes across blocks") {
    newDev().flatMap { d =>
      d.openWrite.use { w =>
        val pat = ByteVector.view(Array.tabulate[Byte](1024)(i => (i % 251).toByte))
        for
          _   <- w.writeBlocks(0, pat)
          got <- w.readBytes(100L, 200L).compile.to(ByteVector)
          _   <- IO(assertEquals(got, pat.slice(100, 300)))
        yield ()
      }
    }
  }

  test("BlockDevice.readOnly causes openWrite to fail") {
    newDev().flatMap { d =>
      val ro = BlockDevice.readOnly(d)
      assert(ro.readOnly)
      ro.openWrite.use_.attempt.map {
        case Left(_: ReadOnlyException) => ()
        case other => fail(s"expected ReadOnlyException, got $other")
      }
    }
  }

  test("BlockDevice.slice exposes only the sub-range") {
    newDev(blocks = 16).flatMap { d =>
      // Write a unique byte into block 5 of the underlying device.
      d.openWrite.use(_.writeBlocks(5, ByteVector.fill(512L)(0xab.toByte))).flatMap { _ =>
        val s = BlockDevice.slice(d, sliceStartBlock = 4, sliceBlocks = 4)
        assertEquals(s.geometry.blocks, 4L)
        s.openRead.use { r =>
          // slice block 1 == underlying block 5
          r.readBlocks(1, 1).map { bv =>
            assert(bv.toArray.forall(_ == 0xab.toByte))
          }
        }
      }
    }
  }

  test("slice rejects out-of-bounds reads") {
    newDev(blocks = 8).flatMap { d =>
      val s = BlockDevice.slice(d, sliceStartBlock = 0, sliceBlocks = 2)
      s.openRead.use { r =>
        IO(intercept[IllegalArgumentException](r.readBlocks(0, 4))).void
      }
    }
  }

  test("BlockDevice.locked serializes I/O without deadlocking") {
    newDev().flatMap { d =>
      BlockDevice.locked[IO](d).use { locked =>
        locked.openWrite.use { w =>
          val payload = ByteVector.fill(512L)(0x5a.toByte)
          for
            _   <- w.writeBlocks(0, payload)
            got <- w.readBlocks(0, 1)
            _   <- IO(assertEquals(got, payload))
          yield ()
        }
      }
    }
  }

  // Forensic-safety: a BlockDeviceReader has no `writeBlocks` member at all.
  // Uncommenting the line below MUST fail to compile.
  //
  //   def attack(r: BlockDeviceReader[IO]) = r.writeBlocks(0, ByteVector.empty)
  //
  // (Not asserted at runtime — the type system *is* the assertion.)
