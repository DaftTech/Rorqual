package com.dafttech.rorqual.core

import munit.FunSuite

class BlockAlignmentSuite extends FunSuite:

  test("offsetInBlock") {
    assertEquals(BlockAlignment.offsetInBlock(0L, 512), 0)
    assertEquals(BlockAlignment.offsetInBlock(511L, 512), 511)
    assertEquals(BlockAlignment.offsetInBlock(512L, 512), 0)
    assertEquals(BlockAlignment.offsetInBlock(513L, 512), 1)
  }

  test("blockOf") {
    assertEquals(BlockAlignment.blockOf(0L, 512), 0L)
    assertEquals(BlockAlignment.blockOf(511L, 512), 0L)
    assertEquals(BlockAlignment.blockOf(512L, 512), 1L)
  }

  test("blockSpan handles aligned, unaligned, and zero-length") {
    assertEquals(BlockAlignment.blockSpan(0L, 512L, 512), 1L)
    assertEquals(BlockAlignment.blockSpan(0L, 1024L, 512), 2L)
    assertEquals(BlockAlignment.blockSpan(100L, 1L, 512), 1L)
    assertEquals(BlockAlignment.blockSpan(500L, 100L, 512), 2L) // crosses a boundary
    assertEquals(BlockAlignment.blockSpan(0L, 0L, 512), 0L)
  }

  test("isAligned") {
    assert(BlockAlignment.isAligned(0L, 512L, 512))
    assert(BlockAlignment.isAligned(1024L, 2048L, 512))
    assert(!BlockAlignment.isAligned(1L, 512L, 512))
    assert(!BlockAlignment.isAligned(0L, 513L, 512))
  }
