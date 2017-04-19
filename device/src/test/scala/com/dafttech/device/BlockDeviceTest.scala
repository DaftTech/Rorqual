package com.dafttech.device

import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 18.04.2017.
  */
object BlockDeviceTest {
  def main(args: Array[String]): Unit = {
    println(Await.result(WindowsBlockDevice.listDeviceDetails.runAsync, Duration.Inf).map(_.mkString("\n")))
  }
}
