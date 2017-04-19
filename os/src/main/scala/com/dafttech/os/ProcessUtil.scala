package com.dafttech.os

import java.io.{BufferedReader, InputStreamReader}

import monix.eval.Task

import scala.concurrent.ExecutionContext

/**
  * Created by pierr on 16.04.2017.
  */
object ProcessUtil {
  def readProcessOutput(command: String*): Task[List[String]] = Task {
    val process = Runtime.getRuntime.exec(command.toArray)
    val outputStream = new BufferedReader(new InputStreamReader(process.getInputStream))
    val output = Stream.continually(Option(outputStream.readLine())).takeWhile(_.isDefined).flatten.toList
    outputStream.close()
    output
  }
}
