package io.estatico.confide.instances

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigList
import io.estatico.confide.FromConf
import shapeless._
import shapeless.labelled.FieldType

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

trait ConfideInstances {

  implicit val hnil: FromConf[HNil] = FromConf.instance((_, _) => HNil)

  implicit def hlist[K <: Symbol, H, T <: HList](
    implicit
    w: Witness.Aux[K],
    cgH: FromConf[H],
    cgT: FromConf[T]
  ): FromConf[FieldType[K, H] :: T] = FromConf.instance((c, p) =>
    labelled.field[K](cgH.get(c, p + "." + w.value.name)) :: cgT.get(c, p)
  )

  implicit val string: FromConf[String] = FromConf.instance2(_.getString)

  implicit val int: FromConf[Int] = FromConf.instance2(_.getInt)

  implicit val number: FromConf[Number] = FromConf.instance2(_.getNumber)

  implicit val float: FromConf[Float] = number.map(_.floatValue)

  implicit val double: FromConf[Double] = number.map(_.doubleValue)

  implicit val boolean: FromConf[Boolean] = FromConf.instance2(_.getBoolean)

  implicit val localTime: FromConf[LocalTime] = string.map(
    LocalTime.parse(_, DateTimeFormatter.ISO_LOCAL_TIME)
  )

  implicit val finiteDuration: FromConf[FiniteDuration] = FromConf.instance((c, p) =>
    FiniteDuration(c.getDuration(p).toNanos, TimeUnit.NANOSECONDS)
  )

  implicit val configList: FromConf[ConfigList] = FromConf.instance2P(_.getList)

  implicit def iter[A : FromConf]: FromConf[Iterator[A]] = FromConf.instance((c, p) =>
    c.getList(p).iterator.asScala.zipWithIndex.map { case (v, i) =>
      FromConf.parseValue(c.origin, s"$p[$i]", FromConf[A].get, v.render)
    }
  )

  implicit def list[A : FromConf]: FromConf[List[A]] = iter[A].map(_.toList)

  implicit def vector[A : FromConf]: FromConf[Vector[A]] = iter[A].map(_.toVector)
}
