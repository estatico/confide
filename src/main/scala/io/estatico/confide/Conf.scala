package io.estatico.confide

import scala.annotation.StaticAnnotation

/**
 * Class macro annotation which tries to automatically derive an instance
 * of [[FromConf]] for a case class and defines it in the companion object.
 * All of the fields of the case class must have an instance of [[FromConf]]
 * for their respective types.
 */
class Conf extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro macros.ConfClassMacroImpl.confClass
}
