package com.dafttech.rorqual.util

import monix.reactive.Observable

import scala.language.implicitConversions

/**
  * Created by pierr on 16.04.2017.
  */
class ParsableObservable[A](val observable: Observable[A]) extends AnyVal {
  def parseWhile[R, B](initial: => R)(f: (R, Option[A]) => (R, Observable[B], Boolean)): Observable[B] =
    observable
      .transform(source => new ParseObservable[A, R, B](source, initial _, f))
      .flatMap(e => e)

  def parse[R, B](initial: => R)(f: (R, A) => (R, Observable[B])): Observable[B] =
    parseWhile(initial) {
      case (state, Some(elem)) =>
        val (newState, result) = f(state, elem)
        (newState, result, false)

      case (state, None) =>
        (state, Observable.empty, true)
    }
}

object ParsableObservable {
  implicit def observable2parsableObservable[A](observable: Observable[A]): ParsableObservable[A] =
    new ParsableObservable[A](observable)
}
