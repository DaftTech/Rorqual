package com.dafttech.device

import com.dafttech.rorqual.WholeBlocks

/**
  * Created by pierr on 27.04.2017.
  */
class DriveStorageHandle(device: FileStorageDevice,
                         writable: Boolean)
  extends FileStorageHandle(device, writable) with WholeBlocks
