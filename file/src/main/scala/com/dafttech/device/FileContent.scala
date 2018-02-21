package com.dafttech.device

import monix.eval.Task
import scodec.bits.ByteVector

trait FileContent {
  def name: String
  def meta: FileMetadata

  def length: Long

  def read(index: Long, length: Long): Task[ByteVector]

  def write(index: Long, data: ByteVector): Task[Unit]
}
