package com.dafttech.rorqual.core

import munit.FunSuite

class BlockGeometrySuite extends FunSuite:

  test("accepts a valid 512n geometry") {
    val g = BlockGeometry(size = 1024L * 512, logicalBlockSize = 512, physicalBlockSize = 512)
    assertEquals(g.blocks, 1024L)
  }

  test("accepts 512e (512 logical / 4096 physical)") {
    val g = BlockGeometry(size = 1024L * 512, logicalBlockSize = 512, physicalBlockSize = 4096)
    assertEquals(g.blocks, 1024L)
  }

  test("rejects non-power-of-2 logicalBlockSize") {
    interceptMessage[IllegalArgumentException]("requirement failed: logicalBlockSize must be a power of 2, got 500") {
      BlockGeometry(size = 5000, logicalBlockSize = 500, physicalBlockSize = 500)
    }
  }

  test("rejects physicalBlockSize smaller than logical") {
    intercept[IllegalArgumentException] {
      BlockGeometry(size = 4096, logicalBlockSize = 4096, physicalBlockSize = 512)
    }
  }

  test("rejects size not aligned to logicalBlockSize") {
    intercept[IllegalArgumentException] {
      BlockGeometry(size = 513, logicalBlockSize = 512, physicalBlockSize = 512)
    }
  }
