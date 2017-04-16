package com.dafttech.rorqual.util

import monix.reactive.Observable

/**
  * Created by pierr on 16.04.2017.
  */
class ParsableObservable[A](val observable: Observable[A]) extends AnyVal {
  def parse[S, E](initial: S)(f: (S, A) => (S, Observable[E])): Observable[E] =
    observable.scan[(S, Observable[E])]((initial, Observable.empty[E])) { (last: (S, Observable[E]), e: A) =>
      f(last._1, e)
    }.flatMap(e => e._2)
}

object ParsableObservable {
  implicit def observable2parsableObservable[A](observable: Observable[A]): ParsableObservable[A] =
    new ParsableObservable[A](observable)
}
