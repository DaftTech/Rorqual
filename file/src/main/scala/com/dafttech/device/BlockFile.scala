package com.dafttech.device

import java.io.RandomAccessFile
import java.nio.file.{Files, Path}

import com.dafttech.rorqual.BlockStorage
import monix.reactive.Observable
import scodec.bits.ByteVector

/**
  * Created by pierr on 09.04.2017.
  */
class BlockFile(path: Path) extends BlockStorage {
  override def size: Long = Files.size(path)

  private val randomAccessFile = new RandomAccessFile(path.toFile, "rw")

  override def read(index: Long, length: Long, chunkSize: Long): Observable[ByteVector] =
    Observable.fromIterable(0 until (size / chunkSize)).map { blockOffset =>
      randomAccessFile.readFully(null, index.toInt, length.toInt)
      ???
    }

  override def write(index: Long, data: Observable[ByteVector]): Unit = ???

  override def close(): Unit = ???
}
