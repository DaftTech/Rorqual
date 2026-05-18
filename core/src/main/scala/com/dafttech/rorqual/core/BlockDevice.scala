package com.dafttech.rorqual.core

import cats.MonadThrow
import cats.effect.kernel.{Concurrent, Resource}

/** A virtual block storage device — the *thing*, separate from an open handle.
  *
  * A device exposes geometry and capability metadata, and two factories for
  * obtaining a read or read-write handle. See [[BlockDeviceReader]] and
  * [[BlockDeviceWriter]] for the I/O surface.
  */
trait BlockDevice[F[_]]:
  def id: BlockDeviceId
  def name: String = id.value
  def geometry: BlockGeometry

  /** True if `openWrite` is guaranteed to fail without trying. */
  def readOnly: Boolean

  /** Acquire a read-only handle. */
  def openRead: Resource[F, BlockDeviceReader[F]]

  /** Acquire a read-write handle. Fails with [[ReadOnlyException]] if [[readOnly]]. */
  def openWrite: Resource[F, BlockDeviceWriter[F]]

object BlockDevice:

  /** Wrap `d` so `openWrite` always fails, regardless of underlying capability. */
  def readOnly[F[_]: MonadThrow](d: BlockDevice[F]): BlockDevice[F] =
    ReadOnlyBlockDevice(d)

  /** A `[sliceStartBlock, sliceStartBlock + sliceBlocks)` view of `d`. */
  def slice[F[_]](d: BlockDevice[F], sliceStartBlock: Long, sliceBlocks: Long): BlockDevice[F] =
    SlicedBlockDevice(d, sliceStartBlock, sliceBlocks)

  /** Wrap `d` so all I/O is serialized through a single mutex.
    *
    * Useful when a backend isn't safe for concurrent access, or when you want
    * exclusive ownership for a critical section.
    */
  def locked[F[_]: Concurrent](d: BlockDevice[F]): Resource[F, BlockDevice[F]] =
    LockedBlockDevice.create(d)
