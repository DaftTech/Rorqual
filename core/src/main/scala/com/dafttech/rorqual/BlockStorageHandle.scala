package com.dafttech.rorqual

import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 08.04.2017.
  */
abstract class BlockStorageHandle(val device: BlockStorageDevice,
                                  val writable: Boolean) extends FileContent {
  override def size: Long = device.size
}
