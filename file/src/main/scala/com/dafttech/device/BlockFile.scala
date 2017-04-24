package com.dafttech.device

import java.io.RandomAccessFile
import java.nio.file.{Files, Path}

import com.dafttech.rorqual.{BlockStorageDevice, BlockStorageHandle}
import monix.eval.Task
import monix.reactive.Observable
import org.lolhens.ifoption.implicits._
import scodec.bits.ByteVector

import scala.annotation.tailrec

/**
  * Created by pierr on 09.04.2017.
  */
class BlockFile(path: Path) extends BlockStorageDevice {
  override val id: String = path.toString

  override def size: Long = Files.size(path)

  override def open(writable: Boolean = false): BlockStorageHandle = new Handle(writable)

  class Handle(writable: Boolean) extends BlockStorageHandle(this) {
    private val randomAccessFile = new RandomAccessFile(path.toFile, s"r${"w" If writable Else ""}s")

    private def read(index: Long, length: Long, a: Boolean): ByteVector = {
      @tailrec
      def fillArray(array: Array[Byte], offset: Int): Long =
        randomAccessFile.read(array, index.toInt + offset, length.toInt - offset) match {
          case -1 => offset
          case bytesRead if bytesRead + offset == length => length
          case bytesRead =>
            println(bytesRead)
            println(offset)
            println(length)
            fillArray(array, bytesRead + offset)
        }

      val array = new Array[Byte](length.toInt)
      val bytesRead = fillArray(array, 0)
      ByteVector(array, 0, bytesRead.toInt)
    }

    override def read(index: Long, length: Long): Observable[ByteVector] =
      Observable.fromIterable(index until (index + length)).bufferTumbling(512).map { chunk =>
        val chunkIndex = chunk.head
        val chunkLength = (chunk.last - chunkIndex) + 1
        read(chunkIndex, chunkLength, false)
      }.takeWhile(_.nonEmpty)

    override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
      data.foldLeftL(index) { (offset, block) =>
        randomAccessFile.write(block.toArray, offset.toInt, block.size.toInt)

        offset + block.size
      }.map(_ => ())

    override def close(): Unit = randomAccessFile.close()
  }

}
