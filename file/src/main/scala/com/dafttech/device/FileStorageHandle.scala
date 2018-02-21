package com.dafttech.device

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

import com.dafttech.rorqual.{AlignedBlockStorage, BlockStorageHandle}
import monix.eval.Task
import org.lolhens.ifoption.implicits._
import scodec.bits.ByteVector

import scala.collection.JavaConverters._

/**
  * Created by pierr on 27.04.2017.
  */
class FileStorageHandle(device: FileStorageDevice,
                        writable: Boolean) extends BlockStorageHandle(device, writable) with AlignedBlockStorage {
  private val fileChannel = FileChannel.open(device.path,
    (Seq(StandardOpenOption.READ, StandardOpenOption.SYNC) ++ (writable Then StandardOpenOption.WRITE)).toSet.asJava)

  override protected def readBlock(index: Long, length: Long): Task[ByteVector] = Task {
    require(length <= Int.MaxValue)
    val buffer = ByteBuffer.allocate(length.toInt)
    fileChannel.read(buffer, index)
    buffer.rewind()
    ByteVector.view(buffer)
  }

  override protected def writeBlock(index: Long, byteVector: ByteVector): Task[Unit] = Task(
    fileChannel.write(byteVector.toByteBuffer, index)
  )

  override def close(): Unit = fileChannel.close()
}
