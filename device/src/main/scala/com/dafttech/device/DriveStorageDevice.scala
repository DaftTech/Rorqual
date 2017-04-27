package com.dafttech.device

import java.nio.file.Path

import com.dafttech.os.OS.{Unix, Windows}
import com.dafttech.os.{OS, Privilege}
import monix.reactive.Observable

/**
  * Created by pierr on 27.04.2017.
  */
class DriveStorageDevice private[device](override val id: String,
                                         override val name: String,
                                         override val size: Long,
                                         override val writable: Boolean,
                                         path: Path) extends FileStorageDevice(path) {

  override def open(writable: Boolean) = {
    if (writable) require(this.writable)
    require(Privilege.isPrivileged)
    new DriveStorageHandle(this, writable)
  }

  override def toString: String = s"""DriveStorageDevice($id, $name, $size, $writable, $path)"""
}

object DriveStorageDevice {
  def list: Observable[DriveStorageDevice] = OS() match {
    case Windows(_) => WindowsBlockDevice.listDevices
    case _: Unix => ???
  }
}
