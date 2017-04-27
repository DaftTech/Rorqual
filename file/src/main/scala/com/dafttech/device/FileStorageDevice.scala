package com.dafttech.device

import java.nio.file.{Files, Path}

import com.dafttech.rorqual.BlockStorageDevice

import scala.util.Try

/**
  * Created by pierr on 09.04.2017.
  */
class FileStorageDevice(val path: Path) extends BlockStorageDevice {
  override val id: String = path.toString

  override def size: Long = Files.size(path)

  override def open(writable: Boolean = false): Try[FileStorageHandle] = Try {
    if (writable) require(this.writable)
    new FileStorageHandle(this, writable)
  }
}
