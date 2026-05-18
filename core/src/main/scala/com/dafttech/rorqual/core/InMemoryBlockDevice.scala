package com.dafttech.rorqual.core

import cats.effect.kernel.{Concurrent, Ref, Resource}
import cats.syntax.all.*
import fs2.{Chunk, Pipe, Stream}
import scodec.bits.ByteVector

import scala.collection.immutable.TreeMap

/** A block device backed by an in-memory sparse map of blocks.
  *
  *   - Writes only allocate the touched blocks.
  *   - Reads from never-written blocks return zeros (sparse-read semantics).
  *   - Two `BlockDevice` instances created from the same `state` share storage.
  */
final class InMemoryBlockDevice[F[_]: Concurrent] private (
    val id: BlockDeviceId,
    val geometry: BlockGeometry,
    state: Ref[F, TreeMap[Long, ByteVector]]
) extends BlockDevice[F]:

  val readOnly: Boolean = false

  def openRead: Resource[F, BlockDeviceReader[F]] =
    Resource.pure(InMemoryBlockDevice.Handle(this, state))

  def openWrite: Resource[F, BlockDeviceWriter[F]] =
    Resource.pure(InMemoryBlockDevice.Handle(this, state))

object InMemoryBlockDevice:

  def apply[F[_]: Concurrent](
      id: String,
      blocks: Long,
      logicalBlockSize: Int = 512,
      physicalBlockSize: Int = 512
  ): F[InMemoryBlockDevice[F]] =
    val geometry = BlockGeometry(
      size = blocks * logicalBlockSize,
      logicalBlockSize = logicalBlockSize,
      physicalBlockSize = physicalBlockSize
    )
    Ref.of[F, TreeMap[Long, ByteVector]](TreeMap.empty).map { ref =>
      new InMemoryBlockDevice(BlockDeviceId(id), geometry, ref)
    }

  private final class Handle[F[_]: Concurrent](
      val device: BlockDevice[F],
      state: Ref[F, TreeMap[Long, ByteVector]]
  ) extends BlockDeviceWriter[F]:

    private val blockSize: Int = device.geometry.logicalBlockSize
    private val zeroBlock: ByteVector = ByteVector.fill(blockSize.toLong)(0)

    private def checkBlockRange(startBlock: Long, count: Long): F[Unit] =
      val end = startBlock + count
      if startBlock < 0 then
        Concurrent[F].raiseError(
          IllegalArgumentException(s"startBlock $startBlock < 0")
        )
      else if end > device.geometry.blocks then
        Concurrent[F].raiseError(
          IllegalArgumentException(
            s"read/write of $count blocks at $startBlock exceeds device " +
              s"(${device.geometry.blocks} blocks)"
          )
        )
      else Concurrent[F].unit

    def readBlocks(startBlock: Long, blockCount: Int): F[ByteVector] =
      checkBlockRange(startBlock, blockCount.toLong) *>
        state.get.map { m =>
          val parts =
            (0 until blockCount).iterator
              .map(i => m.getOrElse(startBlock + i, zeroBlock))
              .toVector
          ByteVector.concat(parts)
        }

    def readBytes(offset: Long, length: Long): Stream[F, Byte] =
      if length <= 0 then Stream.empty
      else
        val firstBlock = offset / blockSize
        val lastBlock  = (offset + length - 1) / blockSize
        val blockCount = (lastBlock - firstBlock + 1).toInt
        val skipHead   = (offset - firstBlock * blockSize).toInt
        Stream
          .eval(readBlocks(firstBlock, blockCount))
          .map(bv => bv.drop(skipHead.toLong).take(length))
          .flatMap(bv => Stream.chunk(Chunk.byteVector(bv)))

    def writeBlocks(startBlock: Long, data: ByteVector): F[Unit] =
      if data.size % blockSize != 0 then
        Concurrent[F].raiseError(
          IllegalArgumentException(
            s"writeBlocks: data.size ${data.size} not a multiple of blockSize $blockSize"
          )
        )
      else
        val count = (data.size / blockSize).toInt
        checkBlockRange(startBlock, count.toLong) *>
          state.update { m =>
            (0 until count).foldLeft(m) { (acc, i) =>
              val block = data.slice(i.toLong * blockSize, (i + 1).toLong * blockSize)
              acc.updated(startBlock + i, block)
            }
          }

    def writeBytesAt(offset: Long): Pipe[F, Byte, Nothing] =
      in =>
        // v0.1: materialize then RMW. Streaming RMW comes later (see ADR-0023).
        val materialize: F[ByteVector] =
          in.chunks.compile
            .fold(ByteVector.empty)((acc, ch) => acc ++ ch.toByteVector)
        Stream.eval(materialize).flatMap { data =>
          if data.isEmpty then Stream.empty
          else Stream.exec(writeAtBytes(offset, data))
        }

    private def writeAtBytes(offset: Long, data: ByteVector): F[Unit] =
      val length     = data.size
      val firstBlock = offset / blockSize
      val lastBlock  = (offset + length - 1) / blockSize
      val blockCount = (lastBlock - firstBlock + 1).toInt
      val headSkip   = (offset - firstBlock * blockSize).toInt
      val tailKeep   = blockCount.toLong * blockSize - (headSkip + length)
      val aligned    = headSkip == 0 && tailKeep == 0

      if aligned then writeBlocks(firstBlock, data)
      else
        readBlocks(firstBlock, blockCount).flatMap { existing =>
          val merged =
            existing.take(headSkip.toLong) ++
              data ++
              existing.drop(headSkip.toLong + length)
          writeBlocks(firstBlock, merged)
        }

    def flush: F[Unit] = Concurrent[F].unit
