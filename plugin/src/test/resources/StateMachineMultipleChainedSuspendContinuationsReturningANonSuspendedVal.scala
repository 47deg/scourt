package continuations {
  final lazy module val compileFromStringpackage:
    continuations.compileFromStringpackage = 
    new continuations.compileFromStringpackage()
  @SourceFile("compileFromStringscala") final module class
    compileFromStringpackage() extends Object() { 
    this: continuations.compileFromStringpackage.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromStringpackage.type])
    private class $fooTest$Frame(private[this] val $completion: continuations.Continuation[Any | Null]) extends 
      continuations.jvm.internal.ContinuationImpl($completion, $completion.context) {
      var I$0: Any = _
      var I$1: Any = _
      var I$2: Any = _
      var I$3: Any = _
      def I$0_=(x$0: Any): Unit = ()
      def I$1_=(x$0: Any): Unit = ()
      def I$2_=(x$0: Any): Unit = ()
      def I$3_=(x$0: Any): Unit = ()
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
        new continuations.jvm.internal.BaseContinuationImpl(completion)
    }
    def fooTest(q: Int, completion: continuations.Continuation[Int]):
      Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
      {
        var q##1: Int = q
        var x: Int = null
        var j: Int = null
        var w: Int = null
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
                println("Hello")
                val z: Int = 100
                $continuation.I$0 = q##1
                $continuation.$label = 1
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  safeContinuation.resume(1.+(z))
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> =>
                      x = orThrow
                      return[label1] ()
                  }
              case 1 =>
                q##1 = $continuation.I$0
                continuations.Continuation.checkResult($continuation.$result)
                x = $continuation.$result.asInstanceOf[Int]
                label1[Unit]: <empty>
                j = 9
                $continuation.I$0 = q##1
                $continuation.I$1 = x
                $continuation.I$2 = j
                $continuation.$label = 2
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  safeContinuation.resume(x.+(1).+(q##1))
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> =>
                      w = orThrow
                      return[label2] ()
                  }
              case 2 =>
                q##1 = $continuation.I$0
                x = $continuation.I$1
                j = $continuation.I$2
                continuations.Continuation.checkResult($continuation.$result)
                w = $continuation.$result.asInstanceOf[Int]
                label2[Unit]: <empty>
                println("World")
                $continuation.I$0 = q##1
                $continuation.I$1 = x
                $continuation.I$2 = j
                $continuation.I$3 = w
                $continuation.$label = 3
                val safeContinuation: continuations.SafeContinuation[Int] = continuations.SafeContinuation.init[Int]($continuation)
                {
                  safeContinuation.resume(x.+(w).+(1).+(j))
                }
                safeContinuation.getOrThrow() match
                  {
                    case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                    case orThrow @ <empty> => ()
                  }
              case 3 =>
                q##1 = $continuation.I$0
                x = $continuation.I$1
                j = $continuation.I$2
                w = $continuation.I$3
                continuations.Continuation.checkResult($continuation.$result)
              case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
            }
        }
        10
      }
  }
}
