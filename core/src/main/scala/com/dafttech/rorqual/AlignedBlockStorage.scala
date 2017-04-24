package com.dafttech.rorqual

import com.dafttech.rorqual.util.ParsableObservable._
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 15.04.2017.
  */
abstract class AlignedBlockStorage(device: BlockStorageDevice) extends BlockStorageHandle(device) {
  def readBlock(index: Long, length: Long): ByteVector

  def writeBlock(index: Long, byteVector: ByteVector): Unit

  override def read(index: Long, length: Long): Observable[ByteVector] = {
    val range = index until (index + length)
    val offset = index % device.blockSize
    val head = range.take(device.blockSize - offset)
    val tail = range.drop(device.blockSize - offset)
    val blocks = head +: tail
  }

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
    device
      .sync(index, data)
      .parse(index)((position, block) => (position + block.size, Observable(position -> block)))
      .foreachL {
        case (i, block) => writeBlock(i, block)
      }
}
