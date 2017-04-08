import java.nio.ByteBuffer

/**
  * Created by fabia on 08.04.2017.
  */
abstract class BlockStorage {
  /**
    * Get size of this storage in bytes
    *
    * @return Size in bytes
    */
  def size: Long

  /**
    * Reads from this storage
    *
    * @param buffer Buffer the data is read to
    * @param index Offset at which to read
    */
  def read(buffer: Array[Byte], index: Long): Unit

  /**
    * Writes to this storage
    *
    * @param buffer Buffer the data is written from
    * @param index Offset at which to write
    */
  def write(buffer: Array[Byte], index: Long): Unit

  /**
    * Checks the boundaries of a IO request.
    *
    * @param index Starting index
    * @param length Length of the IO request
    *
    * @return
    *         0 - No boundaries are violated
    *         1 - index (start) lies outside of this storage
    *         2 - index+length (end) lies outside of this storage
    */
  def checkBoundaries(index: Long, length: Int): Int = {
    if(index < 0 || index >= size) return 1
    if(length < 0) return 2
    if(index + length >= size) return 2

    0
  }

  /**
    * Close this device
    */
  def close()
}
