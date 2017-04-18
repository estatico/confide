package io.estatico.confide.macros

import io.estatico.confide.FromConfObj
import macrocompat.bundle

import scala.reflect.macros.blackbox

@bundle
private[confide] final class ConfClassMacros(val c: blackbox.Context) {

  import c.universe._

  def confClass(annottees: Tree*): Tree = annottees match {
    case List(clsDef: ClassDef) if isCaseClass(clsDef) =>
      q"""
        $clsDef
        object ${clsDef.name.toTermName} {
          ${confGet(clsDef)}
        }
      """

    case List(
    clsDef: ClassDef,
    q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
    ) if isCaseClass(clsDef) =>
      q"""
        $clsDef
        object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf =>
          ..$objDefs
          ..${confGet(clsDef)}
        }
      """

    case _ => c.abort(c.enclosingPosition, s"Only case classes are supported.")
  }

  private val FromConfObjClass = typeOf[FromConfObj[_]].typeSymbol.asType
  private val FromConfObjComp = FromConfObjClass.companion

  private def isCaseClass(clsDef: ClassDef) = clsDef.mods.hasFlag(Flag.CASE)

  private def confGet(clsDef: ClassDef): Tree = {
    if (clsDef.tparams.nonEmpty) c.abort(c.enclosingPosition, s"Type parameters are not supported")
    val typeName = clsDef.name
    val instName = TermName("fromConfObj" + typeName.decodedName)
    q"implicit val $instName: $FromConfObjClass[$typeName] = $FromConfObjComp.derive[$typeName]"
  }
}

