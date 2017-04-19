package io.estatico.confide

import com.typesafe.config.ConfigException.WrongType
import com.typesafe.config.{Config, ConfigFactory, ConfigOrigin}

import scala.util.control.NonFatal

/** Type class for decoding values of type `A` from a `Config`. */
trait FromConf[A] {

  /** Decode the value in `config` at `path` as type `A`. */
  def get(config: Config, path: String): A

  /**
   * Create a new `FromConf` by mapping `f` over a decoded value.
   * If an exception is thrown in in the call to `f`, the exception will
   * be rethrown as a `WrongType`, specifying the path and cause.
   */
  def map[B](f: A => B): FromConf[B] = FromConf.instance((c, p) =>
    try {
      f(get(c, p))
    } catch {
      case NonFatal(e) => throw new WrongType(
        c.origin, s"Failed to get config at path $p: ${e.getMessage}", e
      )
    }
  )
}

object FromConf {

  /** Find an instance of FromConf for A. */
  def apply[A](implicit ev: FromConf[A]): FromConf[A] = ev

  /** Create a new instance of FromConf for A. */
  def instance[A](f: (Config, String) => A): FromConf[A] = new FromConf[A] {
    override def get(config: Config, path: String): A = f(config, path)
  }

  /** Similar to `instance` except expects a curried function. */
  def instance2[A](f: Config => String => A): FromConf[A] = new FromConf[A] {
    override def get(config: Config, path: String): A = f(config)(path)
  }

  /** Instance created using the `getOrParse` strategy. */
  def instanceP[A](f: (Config, String) => A): FromConf[A] = new FromConf[A] {
    override def get(config: Config, path: String): A = getOrParse(config, path, f)
  }

  /** Combines `instance2` and `instanceP`. */
  def instance2P[A](f: Config => String => A): FromConf[A] = new FromConf[A] {
    override def get(config: Config, path: String): A = getOrParse(config, path, f(_)(_))
  }

  /**
   * Attempt to parse a `value` from a String using the `f` function.
   * The `origin` and `path` will only be used for error reporting.
   */
  def parseValue[A](
    origin: ConfigOrigin,
    path: String,
    f: (Config, String) => A,
    value: String
  ): A = {
    try {
      f(ConfigFactory.parseString(s"_: $value"), "_")
    } catch {
      case e: WrongType => throw new WrongType(origin, s"Failed to parse value at $path", e)
    }
  }

  /**
   * Attempt to get value using the function 'f'. If that fails for a WrongType,
   * attempt to parse the value from a string.
   */
  def getOrParse[A](config: Config, path: String, f: (Config, String) => A): A = {
    try {
      f(config, path)
    } catch {
      case _: WrongType => parseValue(config.origin, path, f, config.getString(path))
    }
  }
}
