package continuations {
  import continuations.SuspendApp
  final lazy module val compileFromStringpackage: 
    continuations.compileFromStringpackage = 
    new continuations.compileFromStringpackage()
  @SourceFile("compileFromStringscala") final module class 
    compileFromStringpackage() extends Object() { 
    this: continuations.compileFromStringpackage.type =>
    private def writeReplace(): AnyRef = 
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromStringpackage.type])
    def program: Any =
      {
        private class $foo$Frame($completion: continuations.Continuation[Any | Null]) extends continuations.ContinuationImpl($completion,
          $completion.context) {
          var I$0: Any = _
          def I$0_=(x$0: Any): Unit = ()
          var $result: Either[Throwable, Any | Null | continuations.Continuation.State.Suspended.type] = _
          var $label: Int = _
          def $result_=(x$0: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Unit
             = ()
          def $label_=(x$0: Int): Unit = ()
          protected override def invokeSuspend(
            result: Either[Throwable, Any | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State)]): Any | Null = 
            {
              this.$result = result
              this.$label = this.$label.|(scala.Int.MinValue)
              foo(null, this)
            }
          override def create(value: Any | Null, completion: continuations.Continuation[Any | Null]): continuations.Continuation[Unit] =
            new continuations.BaseContinuationImpl(completion)
        }
        def foo(x: Int, completion: continuations.Continuation[Int]): 
          Int | Null | (continuations.Continuation.State.Suspended : continuations.Continuation.State) =
          {
            var x##1: Int = x
            {
              val $continuation: $foo$Frame =
                completion match 
                  {
                    case x$0 @ x$0:$foo$Frame if x$0.$label.&(scala.Int.MinValue).!=(0) =>
                      x$0.$label = x$0.$label.-(scala.Int.MinValue)
                      x$0
                    case _ => new $foo$Frame(completion)
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
                        safeContinuation.resume(x##1)
                      }
                    }
                    safeContinuation.getOrThrow() match 
                      {
                        case continuations.Continuation.State.Suspended => return continuations.Continuation.State.Suspended
                        case orThrow @ <empty> => ()
                      }
                  case 1 => 
                    x##1 = $continuation.I$0
                    continuations.Continuation.checkResult($continuation.$result)
                  case _ => throw new IllegalArgumentException("call to \'resume\' before \'invoke\' with coroutine")
                }
            }
            10
          }
        continuations.SuspendApp.apply(
          {
            private final class $anon() extends continuations.Starter {
              override def invoke[A](completion: continuations.Continuation[A]): A | Any | Null = foo(11, completion)
            }
            new continuations.Starter {...}
          }
        )
      }
  }
}
