package com.dafttech.device

import com.dafttech.os.ProcessUtil
import monix.eval.Task

import scala.annotation.tailrec

/**
  * Created by pierr on 16.04.2017.
  */
class WindowsBlockDevice {

}

object WindowsBlockDevice {
  def listDevices: Task[List[WindowsBlockDevice]] = {

    ???
  }

  def listDeviceInfo: Task[List[Map[String, String]]] = {
    def parseList(list: List[String]): List[Map[String, String]] = {
      val headerRow = list.head

      val headerParts: List[String] = {
        @tailrec
        def rec(header: String, parts: List[String]): List[String] = if (header == "") parts else {
          val heading = header.takeWhile(_ != ' ')
          val part = heading + header.drop(heading.length).takeWhile(_ == ' ')
          rec(header.drop(part.length), part +: parts)
        }

        rec(headerRow, Nil).reverse
      }

      val headerPartLengths = headerParts.map(_.length)
      val headings = headerParts.map(_.trim)

      val rows = list.tail.filterNot(_.isEmpty).map { row =>
        val rowParts = {
          @tailrec
          def rec(row: String, lengths: List[Int], parts: List[String]): List[String] = if (lengths.isEmpty) parts else
            rec(row.drop(lengths.head), lengths.tail, row.take(lengths.head).trim +: parts)

          rec(row, headerPartLengths, Nil).reverse
        }

        (headings zip rowParts).toMap
      }

      rows
    }

    ProcessUtil.readProcessOutput("wmic", "diskdrive").map(parseList)
  }
}
