package com.dafttech.rorqual

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 24.04.2017.
  */
object ParsableObservableTest {
  def main(args: Array[String]): Unit = {
    object SmallDevice extends BlockStorageDevice {
      override val id: String = "asdf"

      override def size: Long = ???

      override def blockSize = 16

      override def open(writable: Boolean): BlockStorageHandle = ???
    }

    def bvec(size: Int) = ByteVector((0 until size).map(_.toByte))

    val observable: Observable[ByteVector] = Observable(bvec(1), bvec(4), bvec(16))

    val result = SmallDevice.alignAsync(0, observable).map(_.size)
    //println(Await.result(result.toListL.runAsync, Duration.Inf))

    println(Await.result(SmallDevice.align(0, 16).toListL.runAsync, Duration.Inf))
  }
}
