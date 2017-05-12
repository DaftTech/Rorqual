package com.dafttech.device

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import monix.execution.Scheduler.Implicits.global
import org.lolhens.ifoption.implicits._
import scodec.bits.ByteVector

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by pierr on 09.04.2017.
  */
object BlockFileTest {
  def main(args: Array[String]): Unit = {
    def device = new FileStorageDevice(Paths.get("D:\\pierr\\Downloads\\test.txt"))

    val opened = device.open(writable = true).get

    opened.read(0, device.size).foreachL { e =>
      println(e)
      println(e.decodeString(StandardCharsets.ISO_8859_1))
      println("asd")
    }

    val string =
      """asdfjklöasd
        |asdfjklöasd
        |-----------------
        |aaaaaa""".stripMargin

    val byteVector = ByteVector.encodeUtf8(string).get

    Await.result(
      opened.writeBytes(30, byteVector ++ ByteVector.fill(Math.max(0, device.size - byteVector.size - 30))(0)).runAsync,
      Duration.Inf)
  }
}
