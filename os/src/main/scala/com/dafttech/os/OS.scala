package com.dafttech.os

import org.apache.commons.lang3.SystemUtils

/**
  * Created by pierr on 07.04.2017.
  */
class OS(val name: String, val version: String)

object OS {
  def apply(): OS = os

  private lazy val os = (SystemUtils.OS_NAME, Option(SystemUtils.OS_VERSION).getOrElse("")) match {
    case (name, version) if name.startsWith("AIX") => AIX(version)
    case (name, version) if name.startsWith("HP-UX") => HP_UX(version)
    case (name, version) if name.startsWith("OS/400") => OS400(version)
    case (name, version) if name.startsWith("Irix") => Irix(version)
    case (name, version) if name.startsWith("Linux") | name.startsWith("LINUX") => Linux(version)
    case (name, version) if name.startsWith("Mac OS X") => MacOSX(version)
    case (name, version) if name.startsWith("Mac") => Mac(version)
    case (name, version) if name.startsWith("FreeBSD") => FreeBSD(version)
    case (name, version) if name.startsWith("OpenBSD") => OpenBSD(version)
    case (name, version) if name.startsWith("NetBSD") => NetBSD(version)
    case (name, version) if name.startsWith("OS/2") => OS2(version)
    case (name, version) if name.startsWith("Solaris") => Solaris(version)
    case (name, version) if name.startsWith("SunOS") => SunOS(version)
    case (name, version) if name.startsWith("Windows") => Windows(version)
    case (name, version) if name.startsWith("z/OS") => ZOS(version)
    case (name, version) => new OS(name, version)
  }

  class Unix(override val name: String, override val version: String) extends OS(name, version)

  case class AIX(override val version: String) extends Unix("AIX", version)

  case class HP_UX(override val version: String) extends Unix("HP-UX", version)

  case class OS400(override val version: String) extends OS("OS/400", version)

  case class Irix(override val version: String) extends Unix("Irix", version)

  case class Linux(override val version: String) extends Unix("Linux", version)

  case class Mac(override val version: String) extends OS("Mac", version)

  case class MacOSX(override val version: String) extends Unix("Mac OS X", version)

  case class FreeBSD(override val version: String) extends Unix("FreeBSD", version)

  case class OpenBSD(override val version: String) extends Unix("OpenBSD", version)

  case class NetBSD(override val version: String) extends Unix("NetBSD", version)

  case class OS2(override val version: String) extends OS("OS/2", version)

  case class Solaris(override val version: String) extends Unix("Solaris", version)

  case class SunOS(override val version: String) extends Unix("SunOS", version)

  case class Windows(override val version: String) extends OS("Windows", version)

  case class ZOS(override val version: String) extends OS("z/OS", version)

}
