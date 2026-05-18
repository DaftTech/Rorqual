package com.dafttech.rorqual.core

import fs2.Stream
import scodec.bits.ByteVector

/** Read-only capability over a [[BlockDevice]].
  *
  * Code that only needs reads should take a `BlockDeviceReader` rather than a
  * [[BlockDeviceWriter]] — the compiler then guarantees no writes can occur,
  * which is the core of Rorqual's forensic-safety story.
  */
trait BlockDeviceReader[F[_]]:
  def device: BlockDevice[F]

  /** Read `blockCount` contiguous blocks starting at `startBlock`.
    *
    * The returned `ByteVector` has size `blockCount * geometry.logicalBlockSize`.
    * Bounds-check the request against [[BlockGeometry.blocks]] before calling.
    */
  def readBlocks(startBlock: Long, blockCount: Int): F[ByteVector]

  /** Stream `length` bytes from absolute byte `offset`.
    *
    * Internally aligns to block boundaries and slices.
    */
  def readBytes(offset: Long, length: Long): Stream[F, Byte]
