package continuations {
  final lazy module val compileFromStringpackage:
    continuations.compileFromStringpackage = 
    new continuations.compileFromStringpackage()
  @SourceFile("compileFromStringscala") final module class
    compileFromStringpackage() extends Object() { 
    this: continuations.compileFromStringpackage.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromStringpackage.type])
    private class $fooTest$Frame($completion: continuations.Continuation[Any | Null]) extends continuations.ContinuationImpl($completion,
      $completion.context) {
      var I$0: Any = _
      var I$1: Any = _
      def I$0_=(x$0: Any): Unit = ()
      def I$1_=(x$0: Any): Unit = ()
      var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
      var $label: Int = _
      def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit = ()
      def $label_=(x$0: Int): Unit = ()
      protected override def invokeSuspend(
        result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Any | Null =
        {
          this.$result = result
          this.$label = this.$label.|(scala.Int.MinValue)
          continuations.compileFromStringpackage.fooTest(null, this)
        }
      override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
        new continuations.BaseContinuationImpl(completion)
    }
    def fooTest(x: Int, completion: continuations.Continuation[Int]):
      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      {
        var x##1: Int = x
        var y: Int = null
        {
          val $continuation: continuations.compileFromStringpackage.$fooTest$Frame =
            completion match
              {
                case x$0 @ x$0:continuations.compileFromStringpackage.$fooTest$Frame if
                  x$0.$label.&(scala.Int.MinValue).!=(0) =>
                  x$0.$label = x$0.$label.-(scala.Int.MinValue)
                  x$0
                case _ => new continuations.compileFromStringpackage.$fooTest$Frame(completion)
              }
          $continuation.$label match
            {
              case 0 =>
                continuations.Continuation.checkResult($continuation.$result)
                $continuation.I$0 = x##1
                $continuation.$label = 1
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  {
                    safeContinuation.resume(x##1.+(1))
                  }
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> =>
                      y = orThrow
                      return[label1] ()
                  }
              case 1 =>
                x##1 = $continuation.I$0
                continuations.Continuation.checkResult($continuation.$result)
                y = $continuation.$result.asInstanceOf[Int]
                label1[Unit]: <empty>
                $continuation.I$0 = x##1
                $continuation.I$1 = y
                $continuation.$label = 2
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  {
                    safeContinuation.resume(y.+(1))
                  }
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> => orThrow
                  }
              case 2 =>
                x##1 = $continuation.I$0
                y = $continuation.I$1
                continuations.Continuation.checkResult($continuation.$result)
                $continuation.$result
              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
            }
        }
      }
  }
}
