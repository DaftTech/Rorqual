package com.dafttech.rorqual.util

import monix.execution.Ack.{Continue, Stop}
import monix.execution.{Ack, Cancelable, Scheduler}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.util.control.NonFatal

private[util] final class TransformUntilObservable[A, R, B](source: Observable[A],
                                                            initial: () => R,
                                                            f: (R, Next[A]) => (R, Option[B], Boolean))
  extends Observable[B] {

  def unsafeSubscribeFn(out: Subscriber[B]): Cancelable = {
    var streamErrors = true
    try {
      val initialState = initial()
      streamErrors = false

      source.unsafeSubscribeFn(
        new Subscriber.Sync[A] {
          implicit val scheduler: Scheduler = out.scheduler
          private[this] var isDone = false
          private[this] var state: R = initialState

          def onNext(elem: A): Ack = {
            // Protects calls to user code from within the operator,
            // as a matter of contract.
            try {
              val (newState, result, last) = f(state, Next.Elem(elem))
              state = newState
              result.foreach(out.onNext)

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

              val (_, result, _) = f(state, Next.Complete)
              result.foreach(out.onNext)

              out.onComplete()
            }

          def onError(ex: Throwable): Unit =
            if (!isDone) {
              isDone = true

              val (_, result, _) = f(state, Next.Error(ex))
              result.foreach(out.onNext)

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