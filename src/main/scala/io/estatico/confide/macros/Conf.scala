package io.estatico.confide
package macros

import macrocompat.bundle

import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox

/**
 * Class macro annotation which tries to automatically derive an instance
 * of `ConfGet` for a case class and defines it in the companion object.
 * All of the fields of the case class must have an instance of `ConfGet`
 * for their respective types.
 */
class Conf extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ConfClassMacroImpl.confDecoder
}

@bundle
final class ConfClassMacroImpl(val c: blackbox.Context) {

  import c.universe._

  def confDecoder(annottees: Tree*): Tree = annottees match {
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

