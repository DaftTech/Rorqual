package com.dafttech.rorqual

import com.dafttech.rorqual.util.ParsableObservable._
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.runtime.ScalaRunTime

/**
  * Created by pierr on 19.04.2017.
  */
abstract class BlockStorageDevice(val id: String) {
  def name: String

  def size: Long

  def blockSize: Long = 512

  def align(index: Long, data: Observable[ByteVector]): Observable[ByteVector] = {
    data.parse(index) { (last, block) =>

      (last + block.size, Observable.fromIterable(Option(block)))
    }
  }

  override def hashCode(): Int = ScalaRunTime._hashCode(Tuple1(id))

  override def equals(obj: scala.Any): Boolean = obj match {
    case blockStorageDevice: BlockStorageDevice => blockStorageDevice.id == id
    case _ => false
  }
}
