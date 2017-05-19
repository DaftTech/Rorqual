package com.dafttech.rorqual.partition.mbr

import scodec.bits.ByteVector

/**
  * Created by pierr on 19.05.2017.
  */
case class CHS(cylinder: Int, head: Int, sector: Int) {
  require(cylinder >= 0 && cylinder <= 1023, s"Cylinder index $cylinder is invalid!")
  require(head >= 0 && head <= 255, s"Head index $head is invalid!")
  require(sector >= 0 && sector <= 63, s"Sector index $sector is invalid!") // normally 1 - 63

  def invalid: Boolean = cylinder == 0 && head == 0 && sector == 0

  def lba: Long = (cylinder.toLong * CHS.headsPerCylinder + head) * CHS.sectorsPerTrack + sector - 1L

  override def toString: String = s"CHS($cylinder, $head, $sector)=$lba"

  def toBytes: ByteVector = {
    val headByte = ByteVector.fromInt(head, size = 1)
    val sectorBits = ByteVector.fromInt(sector, size = 1).bits.takeRight(6)
    val cylinderBits = ByteVector.fromInt(cylinder, size = 2).bits.takeRight(10)
    headByte ++ (cylinderBits.take(2) ++ sectorBits ++ cylinderBits.drop(2)).bytes
  }
}

object CHS {
  private val headsPerCylinder = 255
  private val sectorsPerTrack = 63

  def fromBytes(bytes: ByteVector): CHS = {
    val bits = bytes.bits
    val head = bytes.take(1)
    val sector = bits.drop(10).take(6).padLeft(8).bytes
    val cylinder = (bits.drop(8).take(2).padLeft(8) ++ bits.drop(16).take(8)).bytes

    println(cylinder.toBin)

    CHS(
      cylinder.toInt(signed = false),
      head.toInt(signed = false),
      sector.toInt(signed = false)
    )
  }

  def fromLba(lba: Long): CHS = {
    val cylinder = lba / (headsPerCylinder * sectorsPerTrack)
    val tmp = lba % (headsPerCylinder * sectorsPerTrack)
    val head = tmp / sectorsPerTrack
    val sector = tmp % sectorsPerTrack + 1

    if (cylinder > 1023 | head > 255 | sector > 63) Max else
      CHS(cylinder.toInt, head.toInt, sector.toInt)
  }

  val Max = CHS(1023, 255, 63)
}
