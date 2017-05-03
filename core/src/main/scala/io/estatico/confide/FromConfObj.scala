package io.estatico.confide

import shapeless.{LabelledGeneric, Lazy}

/**
 * Type class for decoding a ConfigObject directly.
 * Generally useful for decoding case classes and other types which have
 * a `LabelledGeneric` instance from configs.
 */
trait FromConfObj[A] extends FromConf[A] {

  def decodeObject(o: ConfigObject): A

  override def get(config: Config, path: String): A = decodeObject(config.getObject(path))
}

object FromConfObj {

  /** Find an instance of FromConfObj for A. */
  def apply[A](implicit ev: FromConfObj[A]): FromConfObj[A] = ev

  /** Create a new instance of FromConfObj for A. */
  def instance[A](f: ConfigObject => A): FromConfObj[A] = new FromConfObj[A] {
    override def decodeObject(o: ConfigObject): A = f(o)
  }

  /** Derive an instance for a case class. */
  def derive[A](implicit fc: Lazy[DerivedFromConfObj[A]]): FromConfObj[A] = fc.value
}

/**
 * Used internally to simplify calling `FromConf.derive` by only requiring a
 * single type param, inferring the HList representation.
 */
abstract class DerivedFromConfObj[A] extends FromConfObj[A]
object DerivedFromConfObj {
  implicit def derived[A, R](
    implicit
    g: LabelledGeneric.Aux[A, R],
    fc: Lazy[FromConfObj[R]]
  ): DerivedFromConfObj[A] = new DerivedFromConfObj[A] {
    override def decodeObject(o: ConfigObject): A = g.from(fc.value.decodeObject(o))
  }
}
