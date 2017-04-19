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
    val typeName = clsDef.name
    val instName = TermName("fromConfObj" + typeName.decodedName)
    if (clsDef.tparams.isEmpty) {
      q"implicit val $instName: $FromConfObjClass[$typeName] = $FromConfObjComp.derive[$typeName]"
    } else {
      val tparams = clsDef.tparams
      val tparamNames = tparams.map(_.name)
      def mkImplicitParams(typeSymbol: TypeSymbol) =
        tparamNames.map { tparamName =>
          val paramName = c.freshName(tparamName.toTermName)
          val paramType = tq"$typeSymbol[$tparamName]"
          q"$paramName: $paramType"
        }
      val params = mkImplicitParams(FromConfObjClass)
      val fullType = tq"$typeName[..$tparamNames]"
      q"""
        implicit def $instName[..$tparams](implicit ..$params): $FromConfObjClass[$fullType] =
         $FromConfObjComp.derive[$fullType]
      """
    }
  }
}

