package com.dafttech.rorqual.core

/** Pure alignment math, independent of any device or effect type. */
object BlockAlignment:

  /** Byte offset within the block containing `byteOffset`. */
  def offsetInBlock(byteOffset: Long, blockSize: Int): Int =
    (byteOffset % blockSize).toInt

  /** Block index containing `byteOffset`. */
  def blockOf(byteOffset: Long, blockSize: Int): Long =
    byteOffset / blockSize

  /** Block index of the first byte past `byteOffset + length` (exclusive). */
  def lastBlockExclusive(byteOffset: Long, length: Long, blockSize: Int): Long =
    val end = byteOffset + length
    val rem = end % blockSize
    if rem == 0 then end / blockSize else end / blockSize + 1

  /** Number of whole blocks spanned by `[byteOffset, byteOffset + length)`. */
  def blockSpan(byteOffset: Long, length: Long, blockSize: Int): Long =
    if length == 0 then 0L
    else lastBlockExclusive(byteOffset, length, blockSize) - blockOf(byteOffset, blockSize)

  /** True if the request is already aligned to block boundaries. */
  def isAligned(byteOffset: Long, length: Long, blockSize: Int): Boolean =
    byteOffset % blockSize == 0 && length % blockSize == 0
