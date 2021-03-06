package io.estatico.confide.macros

import io.estatico.confide.{FromConf, FromConfObj}
import scala.reflect.macros.blackbox

@macrocompat.bundle
private[confide] final class ConfClassMacros(val c: blackbox.Context) {

  import c.universe._

  def confClass(annottees: Tree*): Tree = annottees match {
    case List(clsDef: ClassDef) =>
      q"""
        $clsDef
        object ${clsDef.name.toTermName} {
          ${fromConfObjInstance(clsDef)}
        }
      """

    case List(
      clsDef: ClassDef,
      q"object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf => ..$objDefs }"
    ) =>
      q"""
        $clsDef
        object $objName extends { ..$objEarlyDefs } with ..$objParents { $objSelf =>
          ..$objDefs
          ${fromConfObjInstance(clsDef)}
        }
      """

    case _ => c.abort(c.enclosingPosition, s"Only case classes are supported.")
  }

  val macroName: Tree = {
    c.prefix.tree match {
      case Apply(Select(New(name), _), _) => name
      case _ => c.abort(c.enclosingPosition, "Unexpected macro application")
    }
  }

  val (debug, debugRaw) = c.prefix.tree match {
    case q"new ${`macroName`}(..$args)" =>
      (
        args.collectFirst { case q"debug = true" => }.isDefined,
        args.collectFirst { case q"debugRaw = true" => }.isDefined
      )
    case _ => (false, false)
  }

  private val FromConfClass = typeOf[FromConf[_]].typeSymbol.asType
  private val FromConfObjClass = typeOf[FromConfObj[_]].typeSymbol.asType
  private val FromConfObjComp = FromConfObjClass.companion

  private def fromConfObjInstance(clsDef: ClassDef): Tree = {
    val typeName = clsDef.name
    val instName = TermName("fromConfObj" + typeName.decodedName)
    val res = if (clsDef.tparams.isEmpty) {
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
      val params = mkImplicitParams(FromConfClass)
      val fullType = tq"$typeName[..$tparamNames]"
      q"""
        implicit def $instName[..$tparams](implicit ..$params): $FromConfObjClass[$fullType] =
         $FromConfObjComp.derive[$fullType]
      """
    }
    if (debug) println(show(res))
    if (debugRaw) println(showRaw(res))
    res
  }
}

