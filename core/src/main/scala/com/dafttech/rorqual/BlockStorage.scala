package com.dafttech.rorqual

import com.dafttech.rorqual.util.ParsableObservable._
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 08.04.2017.
  */
abstract class BlockStorage {
  def name: String

  def id: String

  def size: Long

  def blockSize: Long = 512

  def align(index: Long, data: Observable[ByteVector]): Observable[ByteVector] = {
    data.parse(index) { (last, block) =>

      (last + block.size, Observable.fromIterable(Option(block)))
    }
  }

  def read(index: Long, length: Long): Observable[ByteVector]

  def write(index: Long, data: Observable[ByteVector]): Task[Unit]

  override def finalize(): Unit = close()

  def close(): Unit
}
