package com.dafttech.device

import java.nio.file.Path

import monix.reactive.Observable

/**
  * Created by pierr on 27.04.2017.
  */
class DriveStorageDevice private[device](override val id: String,
                                         override val name: String,
                                         override val size: Long,
                                         path: Path) extends FileStorageDevice(path)

object DriveStorageDevice {
  def list: Observable[DriveStorageDevice] = ???
}
