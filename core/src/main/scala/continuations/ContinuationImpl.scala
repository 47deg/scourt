package continuations

import Continuation.State.*

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

abstract class BaseContinuationImpl(
    val completion: Continuation[Any | Null] | Null
) extends Continuation[Any | Null],
      ContinuationStackFrame,
      Serializable:

  override val executionContext: ExecutionContext =
    if completion == null then throw RuntimeException("resume called with no completion")
    else completion.executionContext
  final override def resume(result: Any | Null): Unit = resumeAux(Right(result))
  final override def raise(error: Throwable): Unit = resumeAux(Left(error))

  private def resumeAux(result: Either[Throwable, Any | Null]): Unit =
    if (completion == null) throw RuntimeException("resume called with no completion")
    else {
      @tailrec
      def go(current: BaseContinuationImpl, param: Either[Throwable, Any | Null]): Unit = {
        val outcome: Either[Throwable, Any | Null] =
          try
            current.invokeSuspend(param) match {
              case Suspended => return
              case o => Right(o)
            }
          catch case exception: Throwable => Left(exception)

        releaseIntercepted()
        completion match
          case base: BaseContinuationImpl =>
            go(base, outcome)
          case _ =>
            outcome.fold(completion.raise, completion.resume)
      }

      go(this, result)
    }

  protected def invokeSuspend(result: Either[Throwable, Any | Null | Suspended.type]): Any |
    Null

  protected def releaseIntercepted(): Unit = ()

  def create(completion: Continuation[?]): Continuation[Unit] =
    throw UnsupportedOperationException("create(Continuation) has not been overridden")

  def create(value: Any | Null, completion: Continuation[Any | Null]): Continuation[Unit] =
    throw UnsupportedOperationException("create(Any?;Continuation) has not been overridden")

  override def callerFrame: ContinuationStackFrame | Null =
    if (completion != null && completion.isInstanceOf[ContinuationStackFrame])
      completion.asInstanceOf
    else null

  override def getStackTraceElement(): StackTraceElement | Null =
    null

/**
 * Named functions with Suspend ?=> A
 */
abstract class ContinuationImpl(
    completion: Continuation[Any | Null],
    override val context: Tuple
) extends BaseContinuationImpl(completion):
  override type Ctx = Tuple
  override val executionContext: ExecutionContext = completion.executionContext
  private var _intercepted: Continuation[Any | Null] = null

  def intercepted(ec: ExecutionContext): Continuation[Any | Null] =
    if (_intercepted != null) _intercepted
    else
      val interceptor = contextService[ContinuationInterceptor]()
      val intercepted =
        if (interceptor != null)
          interceptor.interceptContinuation(
            this
          )
        else this
      _intercepted = intercepted
      intercepted

  override def releaseIntercepted(): Unit =
    val intercepted = _intercepted
    if (intercepted != null && intercepted != this)
      val interceptor = contextService[ContinuationInterceptor]()
      if (interceptor != null)
        interceptor.releaseInterceptedContinuation(intercepted)
      _intercepted = CompletedContinuation

object CompletedContinuation extends Continuation[Any | Null]:
  override type Ctx = Nothing
  override val executionContext: ExecutionContext = ???
  override def context: CompletedContinuation.Ctx =
    throw IllegalStateException("Already completed")
  override def resume(result: Any | Null): Unit =
    throw IllegalStateException("Already completed")
  override def raise(error: Throwable): Unit =
    throw IllegalStateException("Already completed")
