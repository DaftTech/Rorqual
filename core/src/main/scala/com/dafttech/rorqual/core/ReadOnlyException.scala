package com.dafttech.rorqual.core

final class ReadOnlyException(id: BlockDeviceId)
    extends RuntimeException(s"block device ${id.value} is read-only")
