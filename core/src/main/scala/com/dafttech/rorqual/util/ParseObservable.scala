package com.dafttech.rorqual.util

import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.util.control.NonFatal

private[util] final class ParseObservable[A, R](source: Observable[A],
                                                    initial: () => R,
                                                    f: (R, A, Boolean) => (R, Observable[B], Boolean))
  extends Observable[R] {

  def unsafeSubscribeFn(out: Subscriber[R]): Cancelable = {
    var streamErrors = true
    try {
      val initialState = initial()
      streamErrors = false

      source.unsafeSubscribeFn(
        new Subscriber.Sync[A] {
          implicit val scheduler = out.scheduler
          private[this] var isDone = false
          private[this] var state: R = initialState

          def onNext(elem: A): Ack = {
            // Protects calls to user code from within the operator,
            // as a matter of contract.
            try {
              state = f(state, elem)
              Continue
            } catch {
              case NonFatal(ex) =>
                onError(ex)
                Stop
            }
          }

          def onComplete(): Unit =
            if (!isDone) {
              isDone = true
              out.onNext(state)
              out.onComplete()
            }

          def onError(ex: Throwable): Unit =
            if (!isDone) {
              isDone = true
              out.onError(ex)
            }
        })
    }
    catch {
      case NonFatal(ex) if streamErrors =>
        out.onError(ex)
        Cancelable.empty
    }
  }
}