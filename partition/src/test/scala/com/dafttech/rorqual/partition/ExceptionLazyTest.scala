package com.dafttech.rorqual.partition

import scala.util.Try

/**
  * Created by pierr on 18.05.2017.
  */
object ExceptionLazyTest {
  def main(args: Array[String]): Unit = {
    lazy val test: String = throw new RuntimeException("test")

    println("a")

    Try(println(test))

    println(test)

  }
}
