package io.estatico.confide

import com.typesafe.{config => C}

/** Re-exported names from typesafe config for convenience. */
trait TypesafeImports {
  type Config = C.Config
  type ConfigObject = C.ConfigObject
  type ConfigList = C.ConfigList
  type ConfigOrigin = C.ConfigOrigin
  type ConfigException = C.ConfigException
  type WrongTypeConfigException = C.ConfigException.WrongType
}
