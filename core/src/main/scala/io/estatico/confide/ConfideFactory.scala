package io.estatico.confide

import com.typesafe.config.{Config, ConfigFactory}

/** Load configs directly given a `FromConf` instance. */
object ConfideFactory {

  def load[A : FromConfObj](): A = withConfig[A](ConfigFactory.load())

  def load[A : FromConfObj](resourceBaseName: String): A = {
    withConfig[A](ConfigFactory.load(resourceBaseName))
  }

  def parseString[A : FromConfObj](s: String): A = withConfig[A](ConfigFactory.parseString(s))

  def withConfig[A](config: Config)(implicit fc: FromConfObj[A]): A = fc.decodeObject(config.root)
}
