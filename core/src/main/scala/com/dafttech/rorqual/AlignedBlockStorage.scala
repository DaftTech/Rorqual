package com.dafttech.rorqual
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 15.04.2017.
  */
abstract class AlignedBlockStorage extends BlockStorage {
  override def size: Long = ???

  def readBlock(index: Long, length: Long): ByteVector

  def writeBlock(index: Long, byteVector: ByteVector): Unit

  override def read(index: Long, length: Long): Observable[ByteVector] = ???

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] = ???

  override def close(): Unit = ???
}
