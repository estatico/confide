package io.estatico.confide
package macros

import macrocompat.bundle

import scala.reflect.macros.blackbox

@bundle
private[confide] final class ConfClassMacroImpl(val c: blackbox.Context) {

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

  private val FromConfClass = typeOf[FromConf[_]].typeSymbol.asType
  private val FromConfObj = FromConfClass.companion

  private def isCaseClass(clsDef: ClassDef) = clsDef.mods.hasFlag(Flag.CASE)

  private def confGet(clsDef: ClassDef): Tree = {
    if (clsDef.tparams.nonEmpty) c.abort(c.enclosingPosition, s"Type parameters are not supported")
    val typeName = clsDef.name
    val confGetName = TermName("confGet" + typeName.decodedName)
    q"implicit val $confGetName: $FromConfClass[$typeName] = $FromConfObj.derive[$typeName]"
  }
}

