package com.dafttech.rorqual.util

import monix.reactive.Observable

import scala.language.implicitConversions

/**
  * Created by pierr on 16.04.2017.
  */
class ParsableObservable[A](val observable: Observable[A]) extends AnyVal {
  def parseFlatWhile[R, B](initial: => R)(f: (R, Option[A]) => (R, Observable[B], Boolean)): Observable[B] =
    observable
      .transform(source => new ParseObservable[A, R, Observable[B]](source, initial _, f))
      .flatMap(e => e)

  def parseFlat[R, B](initial: => R)(f: (R, A) => (R, Observable[B])): Observable[B] =
    parseFlatWhile(initial) {
      case (state, Some(elem)) =>
        val (newState, result) = f(state, elem)
        (newState, result, false)

      case (state, None) =>
        (state, Observable.empty, true)
    }

  def parseWhile[R, B](initial: => R)(f: (R, Option[A]) => (R, Option[B])): Observable[B] =
    parseFlatWhile(initial) {
      case (state, maybeElem) =>
        val (newState, maybeResult) = f(state, maybeElem)
        (newState, Observable.fromIterable(maybeResult), maybeResult.isEmpty)
    }

  def parse[R, B](initial: => R)(f: (R, A) => (R, B)): Observable[B] =
    parseWhile(initial) {
      case (state, Some(elem)) =>
        val (newState, result) = f(state, elem)
        (newState, Some(result))

      case (state, None) =>
        (state, None)
    }
}

object ParsableObservable {
  implicit def observable2parsableObservable[A](observable: Observable[A]): ParsableObservable[A] =
    new ParsableObservable[A](observable)
}
