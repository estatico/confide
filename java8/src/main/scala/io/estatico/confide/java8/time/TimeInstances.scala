package io.estatico.confide
package java8.time

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Instances for Java 8 time. */
trait TimeInstances {

  implicit val localTime: FromConf[LocalTime] = string.map(
    LocalTime.parse(_, DateTimeFormatter.ISO_LOCAL_TIME)
  )
}
