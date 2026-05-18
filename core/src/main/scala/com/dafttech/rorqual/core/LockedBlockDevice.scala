package com.dafttech.rorqual.core

import cats.effect.kernel.{Concurrent, Resource}
import cats.effect.std.Mutex
import fs2.{Pipe, Stream}
import scodec.bits.ByteVector

/** Serializes all I/O through a single mutex.
  *
  * The mutex is created when the wrapper is acquired and released when it's released —
  * concurrent users of the same `LockedBlockDevice` share the same lock.
  */
private[core] object LockedBlockDevice:

  def create[F[_]: Concurrent](d: BlockDevice[F]): Resource[F, BlockDevice[F]] =
    Resource.eval(Mutex[F]).map(mutex => new Impl(d, mutex))

  private final class Impl[F[_]: Concurrent](underlying: BlockDevice[F], mutex: Mutex[F])
      extends BlockDevice[F]:
    def id: BlockDeviceId    = underlying.id
    def geometry             = underlying.geometry
    def readOnly: Boolean    = underlying.readOnly
    override def name        = underlying.name

    def openRead: Resource[F, BlockDeviceReader[F]] =
      underlying.openRead.map(r => LockedReader(this, r, mutex))

    def openWrite: Resource[F, BlockDeviceWriter[F]] =
      underlying.openWrite.map(w => LockedWriter(this, w, mutex))

  private final class LockedReader[F[_]: Concurrent](
      val device: BlockDevice[F],
      underlying: BlockDeviceReader[F],
      mutex: Mutex[F]
  ) extends BlockDeviceReader[F]:
    def readBlocks(startBlock: Long, blockCount: Int) =
      mutex.lock.surround(underlying.readBlocks(startBlock, blockCount))
    def readBytes(offset: Long, length: Long): Stream[F, Byte] =
      Stream.resource(mutex.lock) >> underlying.readBytes(offset, length)

  private final class LockedWriter[F[_]: Concurrent](
      val device: BlockDevice[F],
      underlying: BlockDeviceWriter[F],
      mutex: Mutex[F]
  ) extends BlockDeviceWriter[F]:
    def readBlocks(startBlock: Long, blockCount: Int) =
      mutex.lock.surround(underlying.readBlocks(startBlock, blockCount))
    def readBytes(offset: Long, length: Long): Stream[F, Byte] =
      Stream.resource(mutex.lock) >> underlying.readBytes(offset, length)
    def writeBlocks(startBlock: Long, data: ByteVector) =
      mutex.lock.surround(underlying.writeBlocks(startBlock, data))
    def writeBytesAt(offset: Long): Pipe[F, Byte, Nothing] =
      in => Stream.resource(mutex.lock) >> underlying.writeBytesAt(offset)(in)
    def flush = mutex.lock.surround(underlying.flush)
