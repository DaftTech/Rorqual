package com.dafttech.rorqual

import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 08.04.2017.
  */
abstract class BlockStorageHandle(val device: BlockStorageDevice,
                                  val writable: Boolean) {

  def read(index: Long, length: Long): Observable[ByteVector]

  def write(index: Long, data: Observable[ByteVector]): Task[Unit]

  def write(index: Long, data: ByteVector): Task[Unit] = write(index, Observable.now(data))

  override def finalize(): Unit = close()

  def close(): Unit
}
