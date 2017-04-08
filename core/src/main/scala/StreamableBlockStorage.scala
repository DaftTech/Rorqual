import monix.reactive.Observable
import scodec.bits.ByteVector

abstract class StreamableBlockStorage {
  def size: Long

  def read(index: Long, length: Long, stripeSize: Int): Observable[ByteVector]
  def write(buffer: ByteVector, index: Long): Unit

  def checkBoundaries(index: Long, length: Int): Int = {
    if(index < 0 || index >= size) return 1
    if(length < 0) return 2
    if(index + length >= size) return 2

    0
  }

  def close()
}
