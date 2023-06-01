package continuations

import dotty.tools.dotc.ast.Trees.ParamClause
import dotty.tools.dotc.ast.{tpd, TreeTypeMap, Trees}
import dotty.tools.dotc.ast.tpd.*
import dotty.tools.dotc.ast.untpd.Modifiers
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.{ctx, Context}
import dotty.tools.dotc.core.{Flags, Names, Scopes, Types}
import dotty.tools.dotc.core.Names.{termName, typeName, Name}
import dotty.tools.dotc.core.StdNames.{nme, tpnme}
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.{MethodType, OrType, PolyType}
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.plugins.StandardPlugin
import dotty.tools.dotc.transform.PickleQuotes
import dotty.tools.dotc.transform.Staging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import dotty.tools.dotc.util.Property.Key
import continuations.ContinuationsCallsPhase.CallerKey
import continuations.ContinuationsCallsPhase.Caller

class ContinuationsPlugin extends StandardPlugin:

  val name: String = "continuations"
  override val description: String = "CPS transformations"

  def init(options: List[String]): List[PluginPhase] =
    (new ContinuationsPhase) :: new ContinuationsCallsPhase :: Nil

class ContinuationsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsPhase.name

  override def changesBaseTypes: Boolean = true

  override def changesMembers: Boolean = true

  override val runsAfter = Set(Staging.name)
  override val runsBefore = Set(ContinuationsCallsPhase.name)

  override def transformValDef(tree: ValDef)(using Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

  override def transformDefDef(tree: DefDef)(using ctx: Context): Tree =
    DefDefTransforms.transformSuspendContinuation(tree)

end ContinuationsPhase

object ContinuationsPhase:
  val name = "continuations"

  /**
   * A unique value type for holding the method undergoing transformation.
   *
   * @param t The method undergoing transformation. Used in
   *   attachments to assist in the transformation.
   */
  case class TransformedMethod(t: tpd.Tree)

  /**
   * A dotty Property.Key[V] class to use to add suspended DefDef
   * attachments to trees during transformation.
   */
  case object TransformedMethodKey extends Key[TransformedMethod]

  /**
    * A unique value type for holding valdefs with suspension points
    * undergoing transformation.
    *
    * @param t The valdef containing the suspension point. Used in
    * attatchments to assist in the transformation.
    */
  case class SuspensionPointValDef(t: tpd.ValDef)

  /**
    * A dotty Property.Key[V] class used to add suspension point
    * attachments to trees during transformation.
    */
  case object SuspensionPointValDefKey extends Key[SuspensionPointValDef]

  /**
    * A unique value type for holding the label number to jump to in
    * suspension points undergoing transformation.
    *
    * @param i The number of the label to jump to.
    */
  case class JumpToLabel(i: Int)

  /**
    * A dotty Property.Key[V] class used to add jump to label
    * attachments to trees during transformation.
    */
  case object JumpToLabelKey extends Key[JumpToLabel]


end ContinuationsPhase

/**
 * Transform calls `foo()` or `foo()(continuations.Suspend.given_Suspend)` to
 * `foo(ContinuationStub.contImpl)` for `def foo()(using s: Suspend)`.
 *
 * In phase `ContinuationsPhase` the `def foo()(using s: Suspend)` has been transformed to `def
 * foo(completion: continuations.Continuation[Int])`.
 */
class ContinuationsCallsPhase extends PluginPhase:

  override def phaseName: String = ContinuationsCallsPhase.name

  override val runsAfter = Set(ContinuationsPhase.name)
  override val runsBefore = Set(PickleQuotes.name)

  private val updatedMethods: mutable.ListBuffer[Symbol] = mutable.ListBuffer.empty
  private val applyToChange: mutable.ListBuffer[Tree] = ListBuffer.empty

  private def existsTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find { s =>
      tree.existsSubTree(t => s.name == t.symbol.name && s.coord == t.symbol.coord)
    }

  private def findTree(tree: Tree)(using Context): Option[Symbol] =
    updatedMethods.find(s => s.name == tree.symbol.name && s.coord == tree.symbol.coord)

  private def treeIsSuspendAndNotInApplyToChange(tree: Apply)(using Context): Boolean =
    tree.filterSubTrees(CallsSuspendParameter.apply).nonEmpty && !applyToChange.exists(
      _.filterSubTrees(_.sameTree(tree)).nonEmpty)

  private def treeExistsIsApplyAndIsNotInApplyToChange(tree: Apply, n: Name)(
      using Context): Boolean =
    existsTree(tree).nonEmpty &&
      n.asTermName == nme.apply &&
      treeIsSuspendAndNotInApplyToChange(tree)

  private def removeSuspend(trees: List[Tree])(using Context): List[Tree] =
    trees.filterNot(_.tpe.hasClassSymbol(requiredClass(suspendFullName)))

  private def treeExistsAndIsMethod(tree: Tree)(using Context): Boolean =
    existsTree(tree).exists(_.is(Flags.Method))

  private def treeExistsIsApplyAndIsMethod(tree: Tree, n: Name)(using Context): Boolean =
    n.asTermName == nme.apply && treeExistsAndIsMethod(tree)

  def hasContinuationParam(tree: DefDef)(using Context): Boolean =
    tree.termParamss.flatten.exists { p =>
      p.tpt.tpe.matches(requiredClassRef(continuationFullName))
    }

  override def prepareForDefDef(tree: DefDef)(using Context): Context =
    if (hasContinuationParam(tree))
      updatedMethods.addOne(tree.symbol)
    tree.foreachSubTree {
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) && tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) && !tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(_, _) if hasContinuationParam(tree) && !tree.symbol.isAnonymousFunction =>
        a.removeAttachment(CallerKey)
        a.putAttachment(CallerKey, Caller(tree))
      case a @ Apply(Apply(_, _), _)
          if existsTree(tree).nonEmpty && treeIsSuspendAndNotInApplyToChange(a) =>
        applyToChange.addOne(a)
      case a @ Apply(Select(_, selected), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(a, selected) =>
        applyToChange.addOne(a)
      case a @ Apply(TypeApply(Select(_, selected), _), _)
          if treeExistsIsApplyAndIsNotInApplyToChange(a, selected) =>
        applyToChange.addOne(a)
      case a @ Apply(Ident(_), _)
          if findTree(a).nonEmpty && treeIsSuspendAndNotInApplyToChange(a) =>
        applyToChange.addOne(a)
      case t =>
        ()
    }
    summon[Context]
  end prepareForDefDef

  def isSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.info.hasClassSymbol(requiredClass(suspendFullName))

  def isGivenSuspend(t: tpd.Tree)(using Context): Boolean =
    t.symbol.originalOwner.showFullName == suspendFullName

  override def transformDefDef(tree: tpd.DefDef)(using Context): tpd.Tree =
    TreeTypeMap(
      treeMap = {
        case tree @ Apply(fn, args) if args.exists(isSuspend) =>
          val caller = tree.getAttachment(CallerKey).flatMap { caller =>
            caller
              .t
              .asInstanceOf[tpd.DefDef]
              .paramss
              .flatten
              .find(_.symbol.info.hasClassSymbol(requiredClass(continuationFullName)))
              .map(_.symbol)
          }
          println(s"caller: ${caller}")
          println(s"existsTree(fn).get: ${existsTree(fn).get}")
          // val replacement = deconstructNestedApply(tree, Nil)
          TreeTypeMap(
            substTo = List(existsTree(fn).get) ++ caller.toList,
            substFrom = List(fn.symbol) ++ args
              .find(_.symbol.info.hasClassSymbol(requiredClass(suspendFullName)))
              .map(_.symbol)
              .toList,
            newOwners = existsTree(fn).toList ++ caller.toList,
            oldOwners = List(fn.symbol) ++ args
              .find(_.symbol.info.hasClassSymbol(requiredClass(suspendFullName)))
              .map(_.symbol.owner)
              .toList
          )(tpd.Apply(fn, args.map{ arg => if(isSuspend(arg)){ ref(caller.get) } else arg }))
        case tree @ Apply(_, _) =>
          tree
        case t => t
      }
    )(tree)
  end transformDefDef

end ContinuationsCallsPhase

object ContinuationsCallsPhase:
  val name = "continuationsCallsPhase"

  /**
   * A unique value class for holding the caller defdef of a suspended definition apply. Knowing
   * the caller allows us to choose the default completion or the caller's completion parameter.
   *
   * @param t
   *   The caller
   */
  case class Caller(t: tpd.Tree)

  /**
   * A dotty Property.Key[V] class to use to add attachments to Apply trees that contain the
   * closest caller to a suspended definition.
   */
  case object CallerKey extends Key[Caller]
