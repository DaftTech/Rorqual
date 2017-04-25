package com.dafttech.rorqual

import scodec.bits.ByteVector

/**
  * Created by pierr on 25.04.2017.
  */
trait WholeBlocks extends AlignedBlockStorage {
  override def readBlock(index: Long, length: Long): ByteVector = {
    val startOffset = index % device.blockSize
    val blockStart = index - startOffset
    val endOffset = (index + length) % device.blockSize
    val blockEnd = (index + length) + device.blockSize - endOffset
    val block = super.readBlock(blockStart, blockEnd - blockStart)
    block // TODO crop
  }

  override def writeBlock(index: Long, byteVector: ByteVector): Unit = {
    // TODO: Maybe read and write back
  }
}
