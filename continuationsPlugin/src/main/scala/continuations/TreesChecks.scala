package continuations

import dotty.tools.dotc.ast.Trees.{Tree => TTree, Untyped}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.core.Contexts.Context

trait TreesChecks extends Trees {

  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check if [[continuations.Suspend#shift]] is called
   * @return
   *   True if the calls calls the method [[continuations.Suspend#shift]]
   */
  private[continuations] def subtreeCallsSuspend(tree: Tree)(using Context): Boolean =
    val treeIsContinuationsSuspendContinuation: Context ?=> Tree => Boolean = t =>
      t.denot.matches(shiftMethod.symbol)

    tree.existsSubTree {
      case Inlined(call, _, _) => treeIsContinuationsSuspendContinuation(call)
      case t => treeIsContinuationsSuspendContinuation(t)
    }

  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check if it calls [[continuations.Suspend#shift]]
   * @return
   *   True if the tree calls the method [[continuations.Suspend#shift]]
   */
  private[continuations] def treeCallsSuspend(tree: Tree)(using Context): Boolean =
    val treeIsContinuationsSuspendContinuation: Context ?=> Tree => Boolean = t =>
      t.denot.matches(shiftMethod.symbol)

    tree match
      case Inlined(call, _, _) => treeIsContinuationsSuspendContinuation(call)
      case _ => false

  /**
   * @param tree
   *   A [[dotty.tools.dotc.ast.tpd.Tree]] to check if it calls
   *   [[continuations.Continuation.resume]]
   * @return
   *   True if the tree calls the method [[continuations.Continuation.resume]]
   */
  private[continuations] def treeCallsResume[A <: Untyped](tree: TTree[A])(
      using Context): Boolean =
    tree.denot.matches(continuationResumeMethod.symbol)

  private[continuations] def valDefTreeCallsSuspend(tree: Tree)(using Context): Boolean =
    tree match
      case vd @ ValDef(_, _, _) => treeCallsSuspend(vd.rhs)
      case _ => false
}
