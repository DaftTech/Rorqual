package com.dafttech.rorqual

import com.dafttech.rorqual.util.ParsableObservable._
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.runtime.ScalaRunTime

/**
  * Created by pierr on 19.04.2017.
  */
abstract class BlockStorageDevice {
  val id: String

  def name: String = id

  def size: Long

  def blockSize: Long = 512

  def writable: Boolean = true

  def open(writable: Boolean = false): BlockStorageHandle

  def sync(index: Long, data: Observable[ByteVector]): Observable[ByteVector] =
    data
      .parse(index) { (position, block) =>
        val offset = position % blockSize
        val head = block.take(blockSize - offset)
        val tail = block.drop(blockSize - offset).grouped(blockSize)
        val blocks = head +: tail

        (position + block.size, Observable.fromIterable(blocks))
      }

  def async(index: Long, data: Observable[ByteVector]): Observable[ByteVector] =
    sync(index, data)
      .parse(index)((position, block) => (position + block.size, Observable(position -> block)))
      .groupBy(_._1 / blockSize)
      .mapTask(_.observable.map(_._2).toListL.map(ByteVector.concat))

  override def hashCode(): Int = ScalaRunTime._hashCode(Tuple1(id))

  override def equals(obj: scala.Any): Boolean = obj match {
    case blockStorageDevice: BlockStorageDevice => blockStorageDevice.id == id
    case _ => false
  }
}
