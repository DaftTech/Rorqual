package com.dafttech.rorqual

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by pierr on 26.04.2017.
  */
object WholeBlocksTest {
  def main(args: Array[String]): Unit = {
    class TestHandle extends BlockStorageHandle(new BlockStorageDevice {
      override def size: Long = 50

      override def open(writable: Boolean): Try[BlockStorageHandle] = ???

      override val id: String = "asdf"
    }, true) with AlignedBlockStorage {


      override def readBlock(index: Long, length: Long): Task[ByteVector] = {
        println(s"read $index $length")
        Task(ByteVector.fill(length)(0))
      }

      override def writeBlock(index: Long, byteVector: ByteVector): Task[Unit] =
        Task.now(println(s"write $index ${byteVector.size}"))

      override def close(): Unit = ???
    }

    val handle = new TestHandle() with WholeBlocks

    println(Await.result(handle.read(10, 503).runAsync, Duration.Inf))
    println("---")
    Await.result(handle.writeBytes(10, Observable(ByteVector.fill(1015)(0.toByte))).runAsync, Duration.Inf)
  }
}
