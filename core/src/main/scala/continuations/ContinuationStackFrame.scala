package continuations

trait ContinuationStackFrame:
  def callerFrame: ContinuationStackFrame | Null
  def getStackTraceElement(): StackTraceElement | Null
