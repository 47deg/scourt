package examples

import continuations.Suspend

import scala.util.Try

@main def ZeroArgumentsTwoContinuationsMultipleResume =
  def zeroArgumentsTwoContinuationsMultipleResume()(using s: Suspend): Int =
    s.shift[Int] { c => c.resume({ println("Resume1"); 1 }) }
    s.shift[Int] { c =>
      c.resume({ println("Resume2"); 1 })
      c.resume({ println("Resume3"); 2 })
    }
  println(Try(zeroArgumentsTwoContinuationsMultipleResume()))
