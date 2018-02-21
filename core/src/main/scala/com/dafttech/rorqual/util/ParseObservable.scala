package com.dafttech.rorqual.util

import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Ack, Cancelable}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.util.control.NonFatal

private[util] final class ParseObservable[A, R, B](source: Observable[A],
                                                   initial: () => R,
                                                   f: (R, Option[A]) => (R, B, Boolean))
  extends Observable[B] {

  def unsafeSubscribeFn(out: Subscriber[B]): Cancelable = {
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
              val (newState, result, last) = f(state, Some(elem))
              state = newState
              out.onNext(result)

              if (last) {
                isDone = true

                out.onComplete()

                Stop
              } else
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

              val (_, result, _) = f(state, None)
              out.onNext(result)

              out.onComplete()
            }

          def onError(ex: Throwable): Unit =
            if (!isDone) {
              isDone = true

              val (_, result, _) = f(state, None)
              out.onNext(result)

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