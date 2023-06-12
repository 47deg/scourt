package continuations

abstract class Starter:
  def invoke[A](completion: Continuation[A]): A | Any | Null

object Starter:
  given Starter = StartMarker

object StartMarker extends Starter:
  override def invoke[A](completion: Continuation[A]): A | Any | Null = ???
