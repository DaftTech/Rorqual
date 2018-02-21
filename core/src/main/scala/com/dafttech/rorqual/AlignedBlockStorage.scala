package com.dafttech.rorqual

import java.nio.file.ReadOnlyFileSystemException

import com.dafttech.rorqual.util.TransformableObservable._
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Aligns all reads and writes to the block boundaries of the device
  *
  * Created by pierr on 15.04.2017.
  */
trait AlignedBlockStorage extends BlockStorageHandle {
  protected def readBlock(index: Long, length: Long): Task[ByteVector]

  protected def writeBlock(index: Long, byteVector: ByteVector): Task[Unit]

  override def readBytes(index: Long, length: Long): Observable[ByteVector] =
    device
      .blockAddresses(index, length)
      .mapTask { block =>
        readBlock(block._1, block._2)
      }

  override def writeBytes(index: Long, data: Observable[ByteVector]): Task[Unit] =
    if (!writable)
      Task.raiseError(new ReadOnlyFileSystemException())
    else
      device
        .alignSync(index, data)
        .mapWithState(index)((position, block) => (position + block.size, position -> block))
        .mergeMap {
          case (i, block) =>
            Observable.fromTask(writeBlock(i, block))
        }
        .foreachL(_ => ())
}
