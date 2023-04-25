package examples

import continuations.Suspend
import continuations.jvm.internal.SuspendApp

@main def OneArgumentsTwoContinuations =
  def oneArgumentsTwoContinuations(x: Int)(using s: Suspend): Int =
    s.shift[Unit] { continuation => continuation.resume(println(x)) }
    s.shift[Int] { continuation => continuation.resume(1) }
  println(SuspendApp(oneArgumentsTwoContinuations(1)))
