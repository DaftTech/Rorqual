package com.dafttech.os

import java.io.PrintStream
import java.util.prefs.Preferences

import scala.util.control.NonFatal

/**
  * Created by pierr on 15.04.2017.
  */
class Privilege {
  lazy val isPrivileged: Boolean = {
    val prefs = Preferences.systemRoot
    val systemErr = System.err
    systemErr synchronized { // better synchroize to avoid problems with other threads that access System.err
      System.setErr(new PrintStream((_: Int) => ()))
      try {
        prefs.put("isprivileged", "true") // SecurityException on Windows
        prefs.remove("isprivileged")
        prefs.flush() // BackingStoreException on Linux
        true
      } catch {
        case NonFatal(_) =>
          false
      } finally
        System.setErr(systemErr)
    }
  }
}
