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

class LockedBlockStorage(handle: BlockStorageHandle) extends BlockStorageHandle(handle.device, true) {
  override def read(index: Long, length: Long): Observable[ByteVector] =
    handle.read(index, length)

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
    handle.write(index, data)

  def unlock(): Unit = ???

  override def close(): Unit = handle.close()
}
