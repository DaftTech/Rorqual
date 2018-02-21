package com.dafttech.rorqual

import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

trait FileContent {
  def size: Long

  def readBytes(index: Long, length: Long): Observable[ByteVector]

  def read(index: Long, length: Long): Task[ByteVector] = readBytes(index, length).toListL.map(ByteVector.concat)

  def writeBytes(index: Long, data: Observable[ByteVector]): Task[Unit]

  def write(index: Long, data: ByteVector): Task[Unit] = writeBytes(index, Observable.now(data))

  def close(): Unit

  override def finalize(): Unit = close()
}
