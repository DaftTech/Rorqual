package com.dafttech.rorqual

import com.dafttech.rorqual.util.TransformableObservable._
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.runtime.ScalaRunTime
import scala.util.Try

/**
  * Created by pierr on 19.04.2017.
  */
abstract class BlockStorageDevice {
  val id: String

  def name: String = id

  def size: Long

  def blockSize: Long = 512

  def writable: Boolean = true

  def open(writable: Boolean = false): Try[BlockStorageHandle]

  /**
    * Returns the block-aligned address ranges for a given address range
    */
  def blockAddresses(index: Long, length: Long): Observable[(Long, Long)] = {
    val offset = index % blockSize
    val remaining = blockSize - offset

    if (length == 0) Observable.empty
    else if (length <= remaining) Observable.now((index, length))
    else Observable.cons(
      (index, remaining),
      Observable.defer(blockAddresses(index + remaining, length - remaining))
    )
  }

  /**
    * Split chunks aligned by the blockSize
    */
  def alignSync(index: Long, data: Observable[ByteVector]): Observable[ByteVector] =
    data
      .flatMapWithState(index) { (position, block) =>
        val offset = position % blockSize
        val remaining = blockSize - offset
        val head = block.take(remaining) // TODO: What if the block is smaller than "remaining"
        val tail = block.drop(remaining).grouped(blockSize)
        val blocks = head +: tail

        (position + block.size, Observable.fromIterable(blocks))
      }

  /**
    * Concat chunks from the same block
    */
  def alignAsync(index: Long, data: Observable[ByteVector]): Observable[ByteVector] =
    alignSync(index, data)
      .mapWithState(index)((position, block) => (position + block.size, position -> block))
      .groupBy(_._1 / blockSize)
      .mapTask(_.observable.map(_._2).toListL.map(ByteVector.concat))

  override def hashCode(): Int = ScalaRunTime._hashCode(Tuple1(id))

  override def equals(obj: scala.Any): Boolean = obj match {
    case blockStorageDevice: BlockStorageDevice => blockStorageDevice.id == id
    case _ => false
  }
}
