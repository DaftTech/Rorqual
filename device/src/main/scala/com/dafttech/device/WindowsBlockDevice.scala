package com.dafttech.device

import java.nio.file.Paths

import com.dafttech.os.ProcessUtil
import monix.reactive.Observable
import org.lolhens.ifoption.implicits._

import scala.annotation.tailrec

/**
  * Created by pierr on 16.04.2017.
  */
private[device] object WindowsBlockDevice {
  def listDevices: Observable[DriveStorageDevice] = (for {
    details <- listDeviceDetails
  } yield {
    val size = details("Size") If_ (_.nonEmpty) Then_ (_.toLong) Else 0L
    val name = details("Caption")
    val id = details("PNPDeviceID")
    val path = {
      val mountPoint = details("Name")
      val prefix = """\\.\PHYSICALDRIVE"""
      require(mountPoint.startsWith(prefix))
      val number = mountPoint.drop(prefix.length)
      //s"\\\\.\\GLOBALROOT\\Device\\Harddisk$number\\Partition0"
      s"\\\\.\\GLOBALROOT\\ArcName\\multi(0)disk(0)rdisk($number)"
    }
    val writable = details("CapabilityDescriptions").contains("Supports Writing")

    new DriveStorageDevice(id, name, size, writable, Paths.get(path))
  }).cache

  def listDeviceDetails: Observable[Map[String, String]] = {
    val rows = ProcessUtil.processOutput("wmic", "diskdrive").cache
    val headRowObservable = rows.headF
    val tailRowsObservable = rows.tail
    val rowValues = for {
      headRow <- headRowObservable
      rowValues <- parseCommandOutputTable(headRow, tailRowsObservable)
    } yield rowValues
    rowValues.cache
  }

  def parseCommandOutputTable(headRow: String, rows: Observable[String]): Observable[Map[String, String]] = {
    val headerParts: List[String] = {
      @tailrec
      def rec(header: String, parts: List[String]): List[String] = if (header == "") parts else {
        val heading = header.takeWhile(_ != ' ')
        val part = heading + header.drop(heading.length).takeWhile(_ == ' ')
        rec(header.drop(part.length), part +: parts)
      }

      rec(headRow, Nil).reverse
    }

    val headerPartLengths = headerParts.map(_.length)
    val headings = headerParts.map(_.trim)

    val rowValues = rows.filter(_.nonEmpty).map { row =>
      val rowParts = {
        @tailrec
        def rec(row: String, lengths: List[Int], parts: List[String]): List[String] = if (lengths.isEmpty) parts else
          rec(row.drop(lengths.head), lengths.tail, row.take(lengths.head).trim +: parts)

        rec(row, headerPartLengths, Nil).reverse
      }

      (headings zip rowParts).toMap
    }

    rowValues
  }
}
