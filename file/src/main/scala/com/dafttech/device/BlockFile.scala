package com.dafttech.device

import java.io.RandomAccessFile
import java.nio.file.{Files, Path}

import com.dafttech.rorqual.BlockStorage
import monix.eval.Task
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 09.04.2017.
  */
class BlockFile(path: Path) extends BlockStorage {
  override def size: Long = Files.size(path)

  private val randomAccessFile = new RandomAccessFile(path.toFile, "rws")

  override def read(index: Long, length: Long, chunkSize: Long): Observable[ByteVector] =
    Observable.fromIterable(index until (index + length)).bufferTumbling(chunkSize.toInt).map { chunk =>
      val chunkIndex = chunk.head
      val chunkLength = chunk.last - chunkIndex
      val chunkArray = new Array[Byte](chunkLength.toInt)
      randomAccessFile.readFully(chunkArray, chunkIndex.toInt, chunkLength.toInt)
      ByteVector(chunkArray)
    }

  override def write(index: Long, data: Observable[ByteVector]): Task[Unit] =
    data.foldLeftL(index) { (offset, block) =>
      randomAccessFile.write(block.toArray, offset.toInt, block.size.toInt)

      offset + block.size
    }.map(_ => ())

  override def close(): Unit = randomAccessFile.close()
}
