package com.dafttech.rorqual.util

import monix.reactive.Observable

import scala.language.implicitConversions

/**
  * Created by pierr on 16.04.2017.
  */
class TransformableObservable[A](val observable: Observable[A]) extends AnyVal {
  def transformUntil[R, B](initial: => R)(f: (R, Next[A]) => (R, Option[B], Boolean)): Observable[B] =
    new TransformUntilObservable[A, R, B](observable, initial _, f)

  def transformWhile[R, B](initial: => R)(f: (R, Next[A]) => (R, Option[B])): Observable[B] =
    transformUntil[R, B](initial) {(state, next)=>
      val (newState, newElem) = f(state, next)
      if (newElem.isDefined)
        (newState, newElem, false)
      else
        (state, None, true)
    }

  def transformFlatUntil[R, B](initial: => R)(f: (R, Next[A]) => (R, Observable[B], Boolean)): Observable[B] =
    transformUntil[R, Observable[B]](initial) { (state, next) =>
      val (newState, newElems, last) = f(state, next)
      (newState, Some(newElems), last)
    }
      .flatten

  def transformFlatWhile[R, B](initial: => R)(f: (R, Next[A]) => (R, Option[Observable[B]])): Observable[B] =
    transformWhile[R, Observable[B]](initial)(f)
      .flatten

  def mapWithState[R, B](initial: => R)(f: (R, A) => (R, B)): Observable[B] =
    transformWhile(initial) {
      case (state, Next.Elem(elem)) =>
        val (newState, newElem) = f(state, elem)
        (newState, Some(newElem))

      case (state, _) =>
        (state, None)
    }

  def flatMapWithState[R, B](initial: => R)(f: (R, A) => (R, Observable[B])): Observable[B] =
    transformFlatUntil(initial) {
      case (state, Next.Elem(elem)) =>
        val (newState, newElems) = f(state, elem)
        (newState, newElems, false)

      case (state, _) =>
        (state, Observable.empty, true)
    }
}

object TransformableObservable {
  implicit def observable2transformableObservable[A](observable: Observable[A]): TransformableObservable[A] =
    new TransformableObservable[A](observable)
}
