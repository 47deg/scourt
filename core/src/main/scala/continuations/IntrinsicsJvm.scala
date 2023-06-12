package continuations

import Continuation.State.*
import scala.concurrent.ExecutionContext

extension [A](continuation: Continuation[A])
  def intercepted(ec: ExecutionContext): Continuation[A] =
    if (continuation.isInstanceOf[ContinuationImpl])
      continuation.asInstanceOf[ContinuationImpl].intercepted(ec).asInstanceOf[Continuation[A]]
    else continuation

extension [A](suspendedFn: Starter ?=> A)

  // inline def shift
  /*
  suspend inline fun <T> suspendCoroutineUninterceptedOrReturn(
      crossinline block: (Continuation<T>) -> Any?
  ): T
   */

  def startContinuation(completion: Continuation[A]): Unit =
    createContinuation(completion).intercepted(completion.executionContext).resume(())

  inline def startContinuationOrSuspend(
      completion: Continuation[A]): Any | Null | Suspended.type =
    suspendedFn.asInstanceOf[Continuation[A] => (Any | Null | Suspended.type)](completion)

  def createContinuation(completion: Continuation[A]): Continuation[Unit] =
    suspendedFn match
      case base: BaseContinuationImpl => base.create(completion)
      case star: Starter => createContinuationFromSuspendFunction(completion, star.invoke)
      case _ => throw new IllegalStateException("Suspended Fn is neither Base nor Starter")

private inline def createContinuationFromSuspendFunction[T](
    completion: Continuation[T],
    block: Continuation[T] => T | Any | Null
): Continuation[Unit] =
  val context = completion.context
  if (context == EmptyTuple)
    new RestrictedContinuation(completion.asInstanceOf) {
      private var label = 0

      override protected def invokeSuspend(
          result: Either[Throwable, Any | Null]): Any | Null | Suspended.type =
        label match
          case 0 =>
            label = 1
            result.void
            block(this)
          case 1 =>
            label = 2
            result.rethrow
          case _ => throw new IllegalStateException("already completed")

    }
  else
    new UnrestrictedContinuationImpl(completion, block)

class UnrestrictedContinuationImpl[T](
    completion: Continuation[T],
    block: Continuation[T] => T | Any | Null
) extends ContinuationImpl(completion.asInstanceOf, completion.context),
      Continuation[Unit] {
  private var label = 0

  override def invokeSuspend(
      result: Either[Throwable, Any | Null | Suspended.type]): Any | Null | Suspended.type =
    label match
      case 0 =>
        label = 1
        result.void
        block(this)
      case 1 =>
        label = 2
        result.rethrow
      case _ => throw new IllegalStateException("already completed")

}
