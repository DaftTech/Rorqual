package com.dafttech.rorqual.core

import cats.effect.kernel.Resource
import fs2.{Pipe, Stream}
import scodec.bits.ByteVector

private[core] final class SlicedBlockDevice[F[_]](
    underlying: BlockDevice[F],
    sliceStartBlock: Long,
    sliceBlocks: Long
) extends BlockDevice[F]:

  require(sliceStartBlock >= 0, s"sliceStartBlock must be >= 0, got $sliceStartBlock")
  require(sliceBlocks >= 0, s"sliceBlocks must be >= 0, got $sliceBlocks")
  require(
    sliceStartBlock + sliceBlocks <= underlying.geometry.blocks,
    s"slice [$sliceStartBlock, ${sliceStartBlock + sliceBlocks}) exceeds underlying device " +
      s"(${underlying.geometry.blocks} blocks)"
  )

  val id: BlockDeviceId =
    BlockDeviceId(s"${underlying.id.value}[$sliceStartBlock..${sliceStartBlock + sliceBlocks})")

  val geometry: BlockGeometry =
    BlockGeometry(
      size = sliceBlocks * underlying.geometry.logicalBlockSize,
      logicalBlockSize = underlying.geometry.logicalBlockSize,
      physicalBlockSize = underlying.geometry.physicalBlockSize
    )

  def readOnly: Boolean = underlying.readOnly

  def openRead: Resource[F, BlockDeviceReader[F]] =
    underlying.openRead.map(r => SlicedReader(this, r, sliceStartBlock))

  def openWrite: Resource[F, BlockDeviceWriter[F]] =
    underlying.openWrite.map(w => SlicedWriter(this, w, sliceStartBlock))

private final class SlicedReader[F[_]](
    val device: BlockDevice[F],
    underlying: BlockDeviceReader[F],
    sliceStartBlock: Long
) extends BlockDeviceReader[F]:

  private val byteOffset = sliceStartBlock * device.geometry.logicalBlockSize

  def readBlocks(startBlock: Long, blockCount: Int) =
    require(startBlock >= 0, s"startBlock must be >= 0, got $startBlock")
    require(
      startBlock + blockCount <= device.geometry.blocks,
      s"read of $blockCount blocks at $startBlock exceeds slice (${device.geometry.blocks} blocks)"
    )
    underlying.readBlocks(sliceStartBlock + startBlock, blockCount)

  def readBytes(offset: Long, length: Long): Stream[F, Byte] =
    require(offset >= 0 && offset + length <= device.geometry.size)
    underlying.readBytes(byteOffset + offset, length)

private final class SlicedWriter[F[_]](
    val device: BlockDevice[F],
    underlying: BlockDeviceWriter[F],
    sliceStartBlock: Long
) extends BlockDeviceWriter[F]:

  private val blockSize  = device.geometry.logicalBlockSize
  private val byteOffset = sliceStartBlock * blockSize

  def readBlocks(startBlock: Long, blockCount: Int) =
    require(startBlock >= 0 && startBlock + blockCount <= device.geometry.blocks)
    underlying.readBlocks(sliceStartBlock + startBlock, blockCount)

  def readBytes(offset: Long, length: Long): Stream[F, Byte] =
    require(offset >= 0 && offset + length <= device.geometry.size)
    underlying.readBytes(byteOffset + offset, length)

  def writeBlocks(startBlock: Long, data: ByteVector) =
    require(startBlock >= 0)
    require(data.size % blockSize == 0, s"write size ${data.size} not a multiple of $blockSize")
    require(startBlock + data.size / blockSize <= device.geometry.blocks)
    underlying.writeBlocks(sliceStartBlock + startBlock, data)

  def writeBytesAt(offset: Long): Pipe[F, Byte, Nothing] =
    underlying.writeBytesAt(byteOffset + offset)

  def flush = underlying.flush
