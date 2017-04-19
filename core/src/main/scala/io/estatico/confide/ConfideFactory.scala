package io.estatico.confide

import com.typesafe.config.ConfigFactory

/** Load configs directly given a `FromConf` instance. */
object ConfideFactory {

  def load[A : FromConfObj](): A = withConfig[A](ConfigFactory.load())

  def load[A : FromConfObj](resourceBaseName: String): A = {
    withConfig[A](ConfigFactory.load(resourceBaseName))
  }

  def parseString[A : FromConfObj](s: String): A = withConfig[A](ConfigFactory.parseString(s))

  def withConfig[A](config: Config)(implicit fc: FromConfObj[A]): A = fc.decodeObject(config.root)

  /** Factory for getting Config values directly without decoding them. */
  object raw {
    def load(): Config = ConfigFactory.load()
    def load(resourceBaseName: String): Config = ConfigFactory.load(resourceBaseName)
    def parseString(s: String): Config = ConfigFactory.parseString(s)
  }
}
