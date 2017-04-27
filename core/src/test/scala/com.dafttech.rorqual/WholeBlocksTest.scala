package com.dafttech.rorqual

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 26.04.2017.
  */
object WholeBlocksTest {
  def main(args: Array[String]): Unit = {
    class TestHandle extends BlockStorageHandle(new BlockStorageDevice {
      override def size: Long = 50

      override def open(writable: Boolean): BlockStorageHandle = ???

      override val id: String = "asdf"
    }, true) with AlignedBlockStorage {


      override def readBlock(index: Long, length: Long): ByteVector = {
        println(s"read $index $length")
        ByteVector.fill(length)(0)
      }

      override def writeBlock(index: Long, byteVector: ByteVector): Unit =
        println(s"write $index ${byteVector.size}")

      override def close(): Unit = ???
    }

    val handle = new TestHandle() with WholeBlocks

    println(Await.result(handle.read(10, 503).toListL.runAsync, Duration.Inf))
    println("---")
    Await.result(handle.write(10, Observable(ByteVector.fill(1015)(0.toByte))).runAsync, Duration.Inf)
  }
}
