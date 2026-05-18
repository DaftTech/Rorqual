package com.dafttech.rorqual.core

final case class BlockGeometry(
    size: Long,
    logicalBlockSize: Int,
    physicalBlockSize: Int
):
  require(size >= 0, s"size must be >= 0, got $size")
  require(
    logicalBlockSize > 0 && (logicalBlockSize & (logicalBlockSize - 1)) == 0,
    s"logicalBlockSize must be a power of 2, got $logicalBlockSize"
  )
  require(
    physicalBlockSize >= logicalBlockSize
      && (physicalBlockSize & (physicalBlockSize - 1)) == 0,
    s"physicalBlockSize must be a power-of-2 >= logicalBlockSize, got $physicalBlockSize"
  )
  require(
    size % logicalBlockSize == 0,
    s"size ($size) must be a multiple of logicalBlockSize ($logicalBlockSize)"
  )

  def blocks: Long = size / logicalBlockSize

object BlockGeometry:
  def of(blocks: Long, logicalBlockSize: Int = 512, physicalBlockSize: Int = 512): BlockGeometry =
    BlockGeometry(blocks * logicalBlockSize, logicalBlockSize, physicalBlockSize)
