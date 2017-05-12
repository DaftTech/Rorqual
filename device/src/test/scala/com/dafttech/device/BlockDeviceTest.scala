package com.dafttech.device

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import scodec.bits.ByteVector

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 18.04.2017.
  */
object BlockDeviceTest {
  def main(args: Array[String]): Unit = {
    val devices = Await.result(DriveStorageDevice.list.toListL.runAsync, Duration.Inf)

    println(devices.map(_.toString).mkString("\n"))

    val testDisk = devices.filter(_.size == 34356994560L).head

    println(testDisk)

    val diskHandle = testDisk.open(writable = true).get

    println(diskHandle)

    val mbr = Await.result(diskHandle.readBytes(0, diskHandle.device.blockSize).runAsync, Duration.Inf)

    println(mbr.toSeq.map(_ & 0xFF).mkString(", "))

    val gpt = Await.result(diskHandle.readBytes(diskHandle.device.blockSize, diskHandle.device.blockSize).runAsync, Duration.Inf)

    println("gpt dec: " + gpt.toSeq.map(_ & 0xFF).mkString(", "))
    println("gpt hex: " + gpt.toSeq.map(e => Integer.toHexString(e & 0xFF)).mkString(", "))

    //Await.result(diskHandle.writeBytes(0, ByteVector.fill(diskHandle.device.blockSize * 2)(0)).runAsync, Duration.Inf)
  }
}
