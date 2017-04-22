package com.dafttech.rorqual.util

import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.language.implicitConversions

/**
  * Created by pierr on 16.04.2017.
  */
class ParsableObservable[A](val observable: Observable[A]) extends AnyVal {
  observable.takeWhile()
  observable.foldLeftL()
  def parse[S, E](initial: S)(f: (S, A, Boolean) => (S, Observable[E], Boolean)): Observable[E] =
    observable.scan[(S, Observable[E])]((initial, Observable.empty[E])) { (last: (S, Observable[E]), e: A) =>
      f(last._1, e, false)
    }.flatMap(e => e._2)
}

object ParsableObservable {
  implicit def observable2parsableObservable[A](observable: Observable[A]): ParsableObservable[A] =
    new ParsableObservable[A](observable)
}
