package com.dafttech.rorqual

import java.nio.file.ReadOnlyFileSystemException

import com.dafttech.rorqual.util.ParsableObservable._
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 15.04.2017.
  */
trait AlignedBlockStorage extends BlockStorageHandle {
  protected def readBlock(index: Long, length: Long): ByteVector

  protected def writeBlock(index: Long, byteVector: ByteVector): Unit

  override def read(index: Long, length: Long): Observable[ByteVector] =
    device
      .align(index, length)
      .map { block =>
        readBlock(block._1, block._2)
      }

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
    if (!writable)
      Task.raiseError(new ReadOnlyFileSystemException())
    else
      device
        .alignSync(index, data)
        .parse(index)((position, block) => (position + block.size, position -> block))
        .foreachL {
          case (i, block) => writeBlock(i, block)
        }
}
