package com.dafttech.rorqual.core

import cats.MonadThrow
import cats.effect.kernel.Resource

private[core] final class ReadOnlyBlockDevice[F[_]](underlying: BlockDevice[F])(using
    F: MonadThrow[F]
) extends BlockDevice[F]:
  def id: BlockDeviceId = underlying.id
  override def name: String = underlying.name
  def geometry: BlockGeometry = underlying.geometry
  def readOnly: Boolean = true

  def openRead: Resource[F, BlockDeviceReader[F]] = underlying.openRead

  def openWrite: Resource[F, BlockDeviceWriter[F]] =
    Resource.eval(F.raiseError(ReadOnlyException(id)))
