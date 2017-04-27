package com.dafttech.os

import java.io.{BufferedReader, InputStreamReader}

import monix.reactive.Observable

/**
  * Created by pierr on 16.04.2017.
  */
object ProcessUtil {
  def processOutput(command: String*): Observable[String] = {
    lazy val outputStream = {
      val process = Runtime.getRuntime.exec(command.toArray)
      new BufferedReader(new InputStreamReader(process.getInputStream))
    }

    Observable.repeatEval(Option(outputStream.readLine()))
      .takeWhile(_.isDefined)
      .map(_.get)
      .doOnTerminate(_ => outputStream.close())
      .cache
  }
}
