package continuations

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import scala.concurrent.ExecutionContext
import Continuation.State.*

class SafeContinuation[T] private (val delegate: Continuation[T])
    extends SafeContinuationBase,
      Continuation[T],
      ContinuationStackFrame:
  override type Ctx = delegate.Ctx
  override val executionContext: ExecutionContext = delegate.executionContext
  override def context: Ctx = delegate.context
  result = Undecided
  private var errored: Boolean = false

  override def resume(value: T): Unit = {
    @tailrec
    def go: Unit = this.result match
      case Undecided =>
        val cas = CAS_RESULT(Undecided, value)
        if cas then () else go
      case Suspended =>
        val cas = CAS_RESULT(Suspended, Resumed)
        if cas then delegate.resume(value) else go
      case _ =>
        throw IllegalStateException("Already resumed")

    go
  }

  override def raise(error: Throwable): Unit = {
    @tailrec
    def go: Unit = this.result match
      case Undecided =>
        val cas = CAS_RESULT(Undecided, error)
        if cas then () else go
      case Suspended =>
        val cas = CAS_RESULT(Suspended, Resumed)
        if (cas) {
          errored = true
          delegate.raise(error)
        } else go
      case _ => throw IllegalStateException("Already resumed")

    go
  }

  def getOrThrow(): T | Null | Suspended.type =
    var result = this.result
    if (result == Undecided) {
      val cas = CAS_RESULT(Undecided, Suspended)
      if cas then return Suspended else result = this.result
    }
    if (result == Resumed) {
      Suspended
    } else if ((result ne null) && errored) {
      throw result.asInstanceOf[Throwable]
    } else
      result.asInstanceOf[T]

  override def callerFrame: ContinuationStackFrame | Null =
    if (delegate != null && delegate.isInstanceOf[ContinuationStackFrame]) delegate.asInstanceOf
    else null

  override def getStackTraceElement(): StackTraceElement | Null =
    null

object SafeContinuation:
  def init[A](cont: Continuation[A]): SafeContinuation[A] =
    import continuations.intercepted
    new SafeContinuation[A](delegate = cont.intercepted(cont.executionContext))
