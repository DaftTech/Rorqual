package com.dafttech.rorqual.util

sealed trait Next[+A]

object Next {

  case object Complete extends Next[Nothing]

  case class Error(ex: Throwable) extends Next[Nothing]

  case class Elem[@specialized(Int, Long, Double, Char, Boolean) +A](elem: A) extends Next[A]

}
