package continuations

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.config.Printers
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.Symbols
import dotty.tools.dotc.core.Symbols.{newSymbol, ClassSymbol}
import dotty.tools.dotc.core.{Flags, Names}
import dotty.tools.dotc.core.Flags.EmptyFlags
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Names.termName
import dotty.tools.dotc.core.Types.*
import munit.FunSuite

class ContinuationsChainedTwoArgs extends FunSuite, CompilerFixtures, StateMachineFixtures {
  val expectedStateMachineTwoContinuationsChained =
    loadFile("StateMachineTwoContinuationsChained")

  compilerContextWithContinuationsPlugin.test(
    "it should convert into a state machine two continuations chained"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val z = s.shift[Int](_.resume(x + y + 1))
           |    s.shift[Int](_.resume(z + 1))
           |}
           |""".stripMargin

      assertCompilesTo(source)(expectedStateMachineTwoContinuationsChained)
  }

  compilerContextWithContinuationsPlugin.test(
    "1- it should convert into a state machine two continuations chained with one generic param"
  ) {
    case given Context =>
      val source =
        """
          |package continuations
          |
          |import continuations.jvm.internal.SuspendApp
          |
          |case class Foo(x: Int)
          |
          |def program = {
          |
          |  def fooTest[A](a: A, b: Int)(using s: Suspend): A = {
          |      val z = s.shift[A] { _.resume(a) }
          |      s.shift[A] { _.resume({ println("World"); z }) }
          |  }
          |
          |  SuspendApp(fooTest(Foo(1), 1))
          |}
          |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedOneGenericParam"))
  }

  compilerContextWithContinuationsPlugin.test(
    "2- it should convert into a state machine two continuations chained with two generic params"
  ) {
    case given Context =>
      val source =
        """
          |package continuations
          |
          |import continuations.jvm.internal.SuspendApp
          |
          |case class Foo(x: Int)
          |case class Bar(x: Int)
          |
          |def program = {
          |
          |  def fooTest[A, B](a: A, b: B)(using s: Suspend): B = {
          |      val z = s.shift[A] { _.resume(a) }
          |      s.shift[B] { _.resume({ println(z); b }) }
          |  }
          |
          |  SuspendApp(fooTest(Foo(1), Bar(2)))
          |}
          |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedTwoGenericParams"))
  }

  compilerContextWithContinuationsPlugin.test(
    "3- it should convert into a state machine two continuations chained with two curried parameters"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int)(y: Int)(using s: Suspend): Int = {
           |    val z = s.shift[Int](_.resume(x + y + 1))
           |    s.shift[Int](_.resume(z + 1))
           |}
           |""".stripMargin

      assertCompilesTo(source)(expectedStateMachineTwoContinuationsChained)
  }

  compilerContextWithContinuationsPlugin.test(
    "4- it should convert into a state machine two continuations chained with one extra given parameter"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |import scala.concurrent.ExecutionContext
           |import concurrent.ExecutionContext.Implicits.global
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend, ec: ExecutionContext): Int = {
           |    val z = s.shift[Int] { _.resume( { x + y + 1 }) }
           |    s.shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedExtraGivenParam"))
  }

  /*
  compilerContextWithContinuationsPlugin.test(
    "5- it should convert context function into a state machine two continuations chained with " +
      "one extra given parameter"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |import scala.concurrent.ExecutionContext
           |import concurrent.ExecutionContext.Implicits.global
           |
           |def fooTest(x: Int, y: Int): (Suspend, ExecutionContext) ?=> Int = {
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + 1 }) }
           |    summon[Suspend].shift[Int] { _.resume({ z + 1 }) }
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineContextFunctionTwoContinuationsChainedExtraGivenParam")

      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "6- it should convert context function into a state machine two continuations chained"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int): Suspend ?=> Int = {
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + 1 }) }
           |    summon[Suspend].shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineContextFunctionTwoContinuationsChained"))
  }

  compilerContextWithContinuationsPlugin.test(
    "7- it should convert into a state machine two continuations chained with one line prior" +
      "to continuations"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    println("Hello")
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + 1 }) }
           |    summon[Suspend].shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedOneLinePrior"))
  }

  compilerContextWithContinuationsPlugin.test(
    "8- it should convert into a state machine two continuations chained with one val prior" +
      "to continuations"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val w = 1
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + w }) }
           |    summon[Suspend].shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedOneValPrior"))
  }

  compilerContextWithContinuationsPlugin.test(
    "9- it should convert into a state machine two continuations chained with two lines prior" +
      "to continuations"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    println("Hello")
           |    println("World")
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + 1 }) }
           |    summon[Suspend].shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoLinesPrior")

      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "10- it should convert into a state machine two continuations chained with two lines prior" +
      "to continuations, one of them is a val"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    println("Hello")
           |    val w = 1
           |    val z = summon[Suspend].shift[Int] { _.resume( { x + y + w }) }
           |    summon[Suspend].shift[Int] { _.resume( { z + 1 }) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoLinesOneValPrior")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "11- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val w = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + w) }
           |    summon[Suspend].shift[Int] { _.resume(z + a) }
           |}
           |""".stripMargin

      assertCompilesTo(source)(loadFile("StateMachineTwoContinuationsChainedTwoValPrior"))
  }

  compilerContextWithContinuationsPlugin.test(
    "12- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations and one line between"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val w = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + w) }
           |    println("Hello")
           |    summon[Suspend].shift[Int] { _.resume(z + a) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoValPriorOneLineBetween")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "13- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations and one val between"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val b = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + a) }
           |    val c = a + 1
           |    summon[Suspend].shift[Int] { _.resume(z + b + c) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoValPriorOneValBetween")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "14- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations and two lines between"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val b = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + a) }
           |    println("Hello")
           |    println("World")
           |    summon[Suspend].shift[Int] { _.resume(z + b) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoLinesBetween")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "15- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations and two lines between, one is a val"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val b = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + a) }
           |    println("Hello")
           |    val c = a + b
           |    summon[Suspend].shift[Int] { _.resume(z + c) }
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoLinesOneValBetween")

      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "16- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations and two val between"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val b = 1
           |    val z = summon[Suspend].shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    summon[Suspend].shift[Int] { _.resume(z + c + d) }
           |}
           |""".stripMargin

      val expected = loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetween")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "17- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations, two val between and one line after"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Unit = {
           |    val a = 1
           |    val b = 1
           |    val z = s.shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    val w = s.shift[Int] { _.resume(z + c + d) }
           |    println(w)
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneLineAfter")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "18- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations, two val between and one val after"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Int = {
           |    val a = 1
           |    val b = 1
           |    val z = s.shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    val w = s.shift[Int] { _.resume(z + c + d) }
           |    val e = w + 1
           |    e
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenOneValAfter")
      assertCompilesTo(source)(expected)
  }
*/
  compilerContextWithContinuationsPlugin.test(
    "19- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations, two val between and 2 lines after"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Unit = {
           |    val a = 1
           |    val b = 1
           |    val z = s.shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    val w = s.shift[Int] { _.resume(z + c + d) }
           |    val e = w + 1
           |    println(e)
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoLinesAfter")
      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "20- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations, two val between and 2 val after"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Unit = {
           |    val a = 1
           |    val b = 1
           |    val z = s.shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    val w = s.shift[Int] { _.resume(z + c + d) }
           |    val e = w + 1
           |    val f = z + w + a
           |    e + f
           |}
           |""".stripMargin

      val expected =
        loadFile("StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfter")

      assertCompilesTo(source)(expected)
  }

  compilerContextWithContinuationsPlugin.test(
    "21- it should convert into a state machine two continuations chained with two val prior" +
      "to continuations, two val between and 2 val after ignoring the chain"
  ) {
    case given Context =>
      val source =
        """|
           |package continuations
           |
           |def fooTest(x: Int, y: Int)(using s: Suspend): Unit = {
           |    val a = 1
           |    val b = 1
           |    val z = s.shift[Int] { _.resume(x + y + a) }
           |    val c = a + b
           |    val d = c + 1
           |    s.shift[Int] { _.resume(z + c + d) }
           |    val e = z + 1
           |    val f = z + a
           |    e + f
           |}
           |""".stripMargin

      val expected = loadFile(
        "StateMachineTwoContinuationsChainedTwoValPriorTwoValBetweenTwoValAfterChainIgnored")

      assertCompilesTo(source)(expected)
  }
}
