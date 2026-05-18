package com.dafttech.rorqual.core

import fs2.Pipe
import scodec.bits.ByteVector

/** Read/write capability over a [[BlockDevice]].
  *
  * Extends [[BlockDeviceReader]] because partial-block writes require read-modify-write.
  * There is no `WriterOnly` because a block device that cannot be read is not a meaningful
  * abstraction.
  */
trait BlockDeviceWriter[F[_]] extends BlockDeviceReader[F]:

  /** Write `data` starting at `startBlock`.
    *
    * `data.size` MUST be a multiple of `device.geometry.logicalBlockSize`. Callers
    * with partial-block data should use [[writeBytesAt]] instead.
    */
  def writeBlocks(startBlock: Long, data: ByteVector): F[Unit]

  /** Sink for writing bytes at absolute byte `offset`.
    *
    * Performs read-modify-write under the hood for any partial blocks at the
    * head or tail of the stream. Concurrent partial writes to the same block
    * are serialized internally.
    */
  def writeBytesAt(offset: Long): Pipe[F, Byte, Nothing]

  /** Force buffered data to durable storage. */
  def flush: F[Unit]
