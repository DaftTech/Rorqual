package com.dafttech.device

import com.dafttech.rorqual.{BlockStorageDevice, BlockStorageHandle}
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 24.04.2017.
  */
class BlockDeviceHandle(device: BlockStorageDevice) extends BlockStorageHandle(device) {
  override def read(index: Long, length: Long): Observable[ByteVector] = ???

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] = ???

  override def close(): Unit = ???
}
