package continuations
package types

import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Symbols.*
import dotty.tools.dotc.core.Types.*

final class DefDefAddCompletionType()(using Context) extends TypeMap {
  def hasSuspendClass(s: Symbol)(using ctx: Context): Boolean =
    s.info.hasClassSymbol(requiredClass(suspendFullName))
  private def makeMethodTypeWithContinuationType(t: Type, continuationType: Type)(using ctx: Context): Type =
    val resultType = t.finalResultType
    val completionType = requiredClassRef(continuationFullName).appliedTo(continuationType)
    MethodType.apply(List(completionType), resultType)

  override def apply(tp: Type): Type =
    tp match {
      case t: TypeRef if hasSuspendClass(t.symbol) =>
        NoType
      case t @ MethodType(termNames) =>
        val resultType = t.finalResultType
        val completionType = requiredClassRef(continuationFullName).appliedTo(resultType)
        val completionParamIndex =
          t.paramInfoss.indexWhere(_.exists(_ =:= requiredClassRef(suspendFullName)))
        val withCompletion = t
          .paramInfoss
          .map(_.map(apply).filterNot(t => {
            t == NoType
          }))
          .filterNot(_.isEmpty)
          .insertAt(completionParamIndex, List(completionType))
        withCompletion.foldRight(resultType)(MethodType.apply)
      case AppliedType(tycon, args) =>
        val argsWithoutSuspend = args.filterNot {
          case tr: TypeRef if hasSuspendClass(tr.symbol) =>
            true
          case _ => false
        }
        val newType =
          if (argsWithoutSuspend.size == 1) argsWithoutSuspend.head
          else
            if (tycon.isContextualMethod) {
              val contextFunctionTypeName = s"scala.ContextFunction${argsWithoutSuspend.size}"
              AppliedType(requiredClassRef(contextFunctionTypeName), argsWithoutSuspend)
            } else AppliedType(tycon, argsWithoutSuspend)
        newType
      case ExprType(resType) =>
        val withoutSuspend = apply(resType)
        val completionType = requiredClassRef(continuationFullName).appliedTo(withoutSuspend)
        MethodType.apply(List(completionType), withoutSuspend)
      case t @ PolyType(lambdaParams, tpe) =>
        PolyType(t.paramNames)(pt => t.paramInfos, pt => apply(tpe))
      case t => t
    }
}
