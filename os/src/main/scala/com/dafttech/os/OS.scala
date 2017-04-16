package com.dafttech.os

import org.apache.commons.lang3.SystemUtils

/**
  * Created by pierr on 07.04.2017.
  */
class OS(val name: String, val version: String)

object OS {
  def apply(): OS = os

  private lazy val os = (SystemUtils.OS_NAME, Option(SystemUtils.OS_VERSION).getOrElse("")) match {
    case (name, version) if name.startsWith("AIX") => new AIX(version)
    case (name, version) if name.startsWith("HP-UX") => new HP_UX(version)
    case (name, version) if name.startsWith("OS/400") => new OS400(version)
    case (name, version) if name.startsWith("Irix") => new Irix(version)
    case (name, version) if name.startsWith("Linux") | name.startsWith("LINUX") => new Linux(version)
    case (name, version) if name.startsWith("Mac OS X") => new MacOSX(version)
    case (name, version) if name.startsWith("Mac") => new Mac(version)
    case (name, version) if name.startsWith("FreeBSD") => new FreeBSD(version)
    case (name, version) if name.startsWith("OpenBSD") => new OpenBSD(version)
    case (name, version) if name.startsWith("NetBSD") => new NetBSD(version)
    case (name, version) if name.startsWith("OS/2") => new OS2(version)
    case (name, version) if name.startsWith("Solaris") => new Solaris(version)
    case (name, version) if name.startsWith("SunOS") => new SunOS(version)
    case (name, version) if name.startsWith("Windows") => new Windows(version)
    case (name, version) if name.startsWith("z/OS") => new ZOS(version)
    case (name, version) => new OS(name, version)
  }

  class Unix(name: String, version: String) extends OS(name, version)

  class AIX(version: String) extends Unix("AIX", version)

  class HP_UX(version: String) extends Unix("HP-UX", version)

  class OS400(version: String) extends OS("OS/400", version)

  class Irix(version: String) extends Unix("Irix", version)

  class Linux(version: String) extends Unix("Linux", version)

  class Mac(version: String) extends OS("Mac", version)

  class MacOSX(version: String) extends Unix("Mac OS X", version)

  class FreeBSD(version: String) extends Unix("FreeBSD", version)

  class OpenBSD(version: String) extends Unix("OpenBSD", version)

  class NetBSD(version: String) extends Unix("NetBSD", version)

  class OS2(version: String) extends OS("OS/2", version)

  class Solaris(version: String) extends Unix("Solaris", version)

  class SunOS(version: String) extends Unix("SunOS", version)

  class Windows(version: String) extends OS("Windows", version)

  class ZOS(version: String) extends OS("z/OS", version)

}
