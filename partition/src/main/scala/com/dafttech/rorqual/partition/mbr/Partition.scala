package com.dafttech.rorqual.partition.mbr

import org.lolhens.ifoption.implicits._
import scodec.bits.{ByteOrdering, ByteVector}

/**
  * Created by pierr on 19.05.2017.
  */
case class Partition(status: Int,
                     startAddress: CHS,
                     partitionType: Int,
                     endAddress: CHS,
                     startSector: Long,
                     sectors: Long) {
  def bootable: Boolean = status == 0x80

  def active: Boolean = status == 0x00

  def invalid: Boolean = !(bootable || active) || startAddress.invalid || endAddress.invalid

  def toBytes: ByteVector =
    ByteVector.fromInt(status, size = 1) ++
      startAddress.toBytes ++
      ByteVector.fromInt(partitionType, size = 1) ++
      endAddress.toBytes ++
      ByteVector.fromLong(startSector, size = 4, ordering = ByteOrdering.LittleEndian) ++
      ByteVector.fromLong(sectors, size = 4, ordering = ByteOrdering.LittleEndian)
}

object Partition {
  def fromBytes(bytes: ByteVector): Option[Partition] = {
    val status = bytes.take(1).toInt(signed = false)
    val partitionType = bytes.drop(4).take(1).toInt(signed = false)
    val startAddress = CHS.fromBytes(bytes.drop(1).take(3))
    val endAddress = CHS.fromBytes(bytes.drop(5).take(3))
    val startSector = bytes.drop(8).take(4).toLong(signed = false, ordering = ByteOrdering.LittleEndian)
    val sectors = bytes.drop(12).take(4).toLong(signed = false, ordering = ByteOrdering.LittleEndian)

    (partitionType != 0) Then
      Partition(status, startAddress, partitionType, endAddress, startSector, sectors)
  }
}
