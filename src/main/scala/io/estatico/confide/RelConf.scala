package io.estatico.confide

import com.typesafe.config.Config

/** Base trait for building objects referring to typesafe config values. */
trait RelConf {

  protected def config: Config

  /** Decode a config value at the specified path. */
  protected def getAtPath[A](path: String)(implicit ev: FromConf[A]): A = ev.get(config, path)

  /** Decode a config value, using the assigned identifier name as the path. */
  protected def get[A : FromConf]: A = macro macros.ConfMacrosImpl.getImpl[A]

  /** Get the assigned identifier name as a string. */
  protected def owner: String = macro macros.ConfMacrosImpl.ownerNameImpl

  /** Alias for [[owner]]. */
  protected def __ : String = macro macros.ConfMacrosImpl.ownerNameImpl

  /** Creates a ConfRef relative to the parent at the specified path. */
  abstract class SubConf(path: String, parent: Config = config) extends RelConf {
    override val config = parent.getConfig(path)
  }
}

