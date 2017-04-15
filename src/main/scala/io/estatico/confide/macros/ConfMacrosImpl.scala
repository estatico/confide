package io.estatico.confide
package macros

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object ConfMacrosImpl {

  /** Uses the identifier name as the path used to lookup a value in the `config` in scope. */
  def getImpl[A](c: blackbox.Context)(confGet: c.Expr[FromConf[A]]): c.Tree = {
    import c.universe._

    val path = ownerNameImpl(c)
    q"$confGet.get(config, $path)"
  }

  /**
   * Get the owner name of the expression as a String;
   * e.g. for `val foo = ...` the result would be "foo".
   */
  def ownerNameImpl(c: blackbox.Context): c.Tree = {
    import c.universe._

    @tailrec
    def extractName(s: Symbol): Name = s.name match {
      case termNames.CONSTRUCTOR => extractName(s.owner.owner)
      case name@TermName(_) => name
      case other => c.abort(c.enclosingPosition, s"Unexpected tree: ${showRaw(other)}")
    }

    val name = extractName(c.internal.enclosingOwner).decodedName.toString.trim
    q"$name"
  }
}
