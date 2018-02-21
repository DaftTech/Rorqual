package com.dafttech.rorqual

import monix.eval.Task
import scodec.bits.ByteVector

import scala.language.postfixOps

/**
  * Only whole blocks are read and written. If half a block should be written the block has to be read first.
  *
  * Created by pierr on 25.04.2017.
  */
trait WholeBlocks extends AlignedBlockStorage {
  abstract override def readBlock(index: Long, length: Long): Task[ByteVector] = {
    val startOffset = index % device.blockSize
    val blockStart = index - startOffset
    val endOffset = device.blockSize - (((index + length - 1) % device.blockSize) + 1)
    val blockEnd = (index + length) + endOffset

    for {
      block <- super.readBlock(blockStart, blockEnd - blockStart)
    } yield
      block.drop(startOffset).take(length)
  }

  abstract override def writeBlock(index: Long, byteVector: ByteVector): Task[Unit] = {
    val startOffset = index % device.blockSize
    val blockStart = index - startOffset
    val endOffset = device.blockSize - (((index + byteVector.size - 1) % device.blockSize) + 1)
    val blockEnd = (index + byteVector.size) + endOffset

    for {
      fullBlock <-
        if (startOffset > 0 || endOffset > 0) {
          for {
            prevBlock <- readBlock(blockStart, blockEnd - blockStart)
          } yield
            prevBlock.take(startOffset) ++ byteVector ++ prevBlock.takeRight(endOffset)
        } else
          Task.now(byteVector)
    } yield
      super.writeBlock(blockStart, fullBlock)
  }
}
