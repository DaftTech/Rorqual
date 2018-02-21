package com.dafttech.rorqual.partition

import com.dafttech.device.DriveStorageDevice
import com.dafttech.rorqual.partition.mbr.{CHS, MBR}
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 18.05.2017.
  */
object MBRTest {
  def main(args: Array[String]): Unit = {
    val devices = Await.result(DriveStorageDevice.list.toListL.runAsync, Duration.Inf)

    println(devices.map(_.toString).mkString("\n"))

    val testDisk = devices.filter(_.size == 36503792640L).head
    //val testDisk = devices.filter(_.size == 128849011200L).head //BOOT DISK!

    println(testDisk)

    val diskHandle = testDisk.open(writable = false).get

    println(diskHandle)

    val mbr = Await.result(diskHandle.read(0, diskHandle.device.blockSize).runAsync, Duration.Inf)

    println(mbr.toSeq.map(_ & 0xFF).mkString(", "))

    val gpt = Await.result(diskHandle.read(diskHandle.device.blockSize, diskHandle.device.blockSize).runAsync, Duration.Inf)

    println("gpt dec: " + gpt.toSeq.map(_ & 0xFF).mkString(", "))
    println("gpt hex: " + gpt.toSeq.map(e => Integer.toHexString(e & 0xFF)).mkString(", "))

    println(MBR.fromBytes(mbr))

    println(CHS.fromBytes(CHS.fromLba(13456789).toBytes).lba)

    //Await.result(diskHandle.writeBytes(0, ByteVector.fill(diskHandle.device.blockSize * 2)(0)).runAsync, Duration.Inf)
  }
}
