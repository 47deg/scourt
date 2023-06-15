package continuations {
  import continuations.jvm.internal.SuspendApp
  final lazy module val compileFromString$package:
    continuations.compileFromString$package
   = new continuations.compileFromString$package()
  @SourceFile("compileFromString.scala") final module class
    compileFromString$package
  () extends Object() { this: continuations.compileFromString$package.type =>
    private def writeReplace(): AnyRef =
      new scala.runtime.ModuleSerializationProxy(classOf[continuations.compileFromString$package.type])
    def program: Any =
      {
        case class Foo(i: Int) extends Object(), _root_.scala.Product, _root_.scala.Serializable {
          override def hashCode(): Int = 
            {
              var acc: Int = -889275714
              acc = scala.runtime.Statics#mix(acc, this.productPrefix.hashCode())
              acc = scala.runtime.Statics#mix(acc, Foo.this.i)
              scala.runtime.Statics#finalizeHash(acc, 1)
            }
          override def equals(x$0: Any): Boolean = 
            this.eq(x$0.$asInstanceOf[Object]).||(
              x$0 match 
                {
                  case x$0 @ _:Foo @unchecked => this.i.==(x$0.i).&&(x$0.canEqual(this))
                  case _ => false
                }
            )
          override def toString(): String = scala.runtime.ScalaRunTime._toString(this)
          override def canEqual(that: Any): Boolean = that.isInstanceOf[Foo @unchecked]
          override def productArity: Int = 1
          override def productPrefix: String = "Foo"
          override def productElement(n: Int): Any = 
            n match 
              {
                case 0 => this._1
                case _ => throw new IndexOutOfBoundsException(n.toString())
              }
          override def productElementName(n: Int): String = 
            n match 
              {
                case 0 => "i"
                case _ => throw new IndexOutOfBoundsException(n.toString())
              }
          val i: Int
          def copy(i: Int): Foo = new Foo(i)
          def copy$default$1: Int @uncheckedVariance = Foo.this.i
          def _1: Int = this.i
        }
        final lazy module val Foo: Foo = new Foo()
        final module class Foo() extends AnyRef(), scala.deriving.Mirror.Product { this: Foo.type =>
          def apply(i: Int): Foo = new Foo(i)
          def unapply(x$1: Foo): Foo = x$1
          override def toString: String = "Foo"
          type MirroredMonoType = Foo
          def fromProduct(x$0: Product): continuations.compileFromString$package.Foo.MirroredMonoType =
            new Foo(x$0.productElement(0).$asInstanceOf[Int])
        }
        def foo(a: A, completion: continuations.Continuation[A]): A | Null | continuations.Continuation.State.Suspended.type = a
        continuations.jvm.internal.SuspendApp.apply(
          {
            private final class $anon() extends continuations.jvm.internal.Starter {
              override def invoke[A](completion: continuations.Continuation[A]): A | Any | Null = foo(Foo.apply(1), completion)
            }
            new continuations.jvm.internal.Starter {...}
          }
        )
      }
  }
}
