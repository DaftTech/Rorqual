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

  def open(writable: Boolean = false): BlockStorageHandle

  def align(index: Long, data: Observable[ByteVector]): Observable[ByteVector] = {
    val split = data.parse(index) {
      case (position, block) =>
        val offset = position % blockSize
        val head = block.take(blockSize - offset)
        val tail = block.drop(blockSize - offset).grouped(blockSize)
        val chunks = head +: tail

        (position + block.size, Observable.fromIterable(chunks))
    }

    val fuse = split
      .parse(index)((position, block) =>
        (position + block.size, Observable(position -> block))
      )
      .groupBy(_._1 / blockSize)
      .mapTask(_.observable.map(_._2).toListL.map(ByteVector.concat))

    fuse
  }

  override def hashCode(): Int = ScalaRunTime._hashCode(Tuple1(id))

  override def equals(obj: scala.Any): Boolean = obj match {
    case blockStorageDevice: BlockStorageDevice => blockStorageDevice.id == id
    case _ => false
  }
}
