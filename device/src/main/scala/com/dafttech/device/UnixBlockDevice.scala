package com.dafttech.device

import java.nio.file.Paths

import com.dafttech.device.WindowsBlockDevice.parseCommandOutputTable
import org.lolhens.ifoption.implicits._
import com.dafttech.os.ProcessUtil
import monix.reactive.Observable

/**
  * Created by pierr on 13.05.2017.
  */
private[device] object UnixBlockDevice {
  def listDevices: Observable[DriveStorageDevice] = (for {
    details <- listDeviceDetails
    if details("TYPE") == "disk"
  } yield {
    val size = details("SIZE") If_ (_.nonEmpty) Then_ (_.toLong) Else 0L
    val name = details("KNAME")
    val path = s"/dev/$name"
    val writable = details("RO").toInt == 0

    new DriveStorageDevice(name, name, size, writable, Paths.get(path))
  }).cache

  def listDeviceDetails: Observable[Map[String, String]] = {
    val rows = ProcessUtil.processOutput("lsblk", "-b", "-io", "KNAME,TYPE,SIZE,RO").cache
    val headRowObservable = rows.headF
    val tailRowsObservable = rows.tail
    val rowValues = for {
      headRow <- headRowObservable
      rowValues <- parseCommandOutputTable(headRow, tailRowsObservable)
    } yield rowValues
    rowValues.cache
  }
}
