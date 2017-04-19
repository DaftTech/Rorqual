package com.dafttech.rorqual

import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 08.04.2017.
  */
trait LockableBlockStorage extends BlockStorageHandle {
  def lock(): LockedBlockStorage
}

class LockedBlockStorage(blockStorage: BlockStorageHandle) extends BlockStorageHandle {
  override def size: Long = blockStorage.size

  override def blockSize = blockStorage.blockSize

  override def read(index: Long, length: Long): Observable[ByteVector] =
    blockStorage.read(index, length)

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
    blockStorage.write(index, data)

  def unlock(): Unit = ???

  override def close(): Unit = blockStorage.close()

  override def name: String = ???

  override def id: String = ???
}
