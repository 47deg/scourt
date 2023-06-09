package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def ZeroArgumentsCodeBefore =
  def zeroArgumentsCodeBefore()(using Suspend): Int =
    println("Hello")
    val x = 1
    summon[Suspend].shift[Unit] { continuation => continuation.resume(println(1)) }
    summon[Suspend].shift[Int] { continuation => continuation.resume(2) }
  println(SuspendApp(zeroArgumentsCodeBefore()))
