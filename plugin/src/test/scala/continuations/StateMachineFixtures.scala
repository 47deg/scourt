package continuations

import CompilerFixtures.removeLineTrailingSpaces
import scala.io.Source

trait StateMachineFixtures {

  def loadFile(file: String): String =
    removeLineTrailingSpaces(Source.fromResource(s"$file.scala").mkString)

  val expectedOneSuspendContinuation =
    loadFile("OneSuspendContinuation")

  val expectedOneSuspendContinuationTwoBlocks =
    loadFile("OneSuspendContinuationTwoBlocks")

  val expectedOneSuspendContinuationThreeBlocks =
    loadFile("OneSuspendContinuationThreeBlocks")

  val expectedOneSuspendContinuationFourBlocks =
    loadFile("OneSuspendContinuationFourBlocks")

}
