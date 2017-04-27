package com.dafttech.rorqual

import scodec.bits.ByteVector
import org.lolhens.ifoption.implicits._

import scala.language.postfixOps

/**
  * Created by pierr on 25.04.2017.
  */
trait WholeBlocks extends AlignedBlockStorage {
  abstract override def readBlock(index: Long, length: Long): ByteVector = {
    val startOffset = index % device.blockSize
    val blockStart = index - startOffset
    val endOffset = device.blockSize - (((index + length - 1) % device.blockSize) + 1)
    val blockEnd = (index + length) + endOffset

    val block = super.readBlock(blockStart, blockEnd - blockStart)
    block.drop(startOffset).take(length)
  }

  abstract override def writeBlock(index: Long, byteVector: ByteVector): Unit = {
    val startOffset = index % device.blockSize
    val blockStart = index - startOffset
    val endOffset = device.blockSize - (((index + byteVector.size - 1) % device.blockSize) + 1)
    val blockEnd = (index + byteVector.size) + endOffset

    val fullBlock =
      if (startOffset > 0 || endOffset > 0) {
        val prevBlock = readBlock(blockStart, blockEnd - blockStart)
        prevBlock.take(startOffset) ++ byteVector ++ prevBlock.takeRight(endOffset)
      } else
        byteVector

    super.writeBlock(blockStart, fullBlock)
  }
}
