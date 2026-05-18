package com.dafttech.rorqual.core

opaque type BlockDeviceId = String

object BlockDeviceId:
  def apply(s: String): BlockDeviceId = s
  extension (id: BlockDeviceId) def value: String = id
