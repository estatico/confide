package io.estatico.confide

import scala.annotation.StaticAnnotation

/**
 * Type macro annotation which tries to automatically derive an instance
 * of `FromConfObj` from the annotated type's `shapeless.LabelledGeneric` instance. The
 * macro will define the `FromConfObj` instance in the companion object of the
 * annotated type.
 *
 * The most general use case will be to annotate a case class, e.g.
 * {{{
 *   @Conf final case class Foo(name: String, age: Int)
 * }}}
 * So long as all of the types of the fields of the case class have a `FromConf` instance,
 * a `shapeless.LabelledGeneric` can be derived and, thus, a `FromConfObj` for the
 * annotated case class.
 */
class Conf extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro macros.ConfClassMacros.confClass
}
