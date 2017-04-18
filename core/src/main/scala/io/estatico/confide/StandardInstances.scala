package io.estatico.confide

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigList
import shapeless._
import shapeless.labelled.FieldType

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

trait StandardInstances {

  implicit val hnil: FromConfObj[HNil] = FromConfObj.instance(_ => HNil)

  implicit def hlist[K <: Symbol, H, T <: HList](
    implicit
    w: Witness.Aux[K],
    fcH: FromConf[H],
    fcT: FromConfObj[T]
  ): FromConfObj[FieldType[K, H] :: T] = FromConfObj.instance(o =>
    labelled.field[K](fcH.get(o.toConfig, w.value.name)) :: fcT.decodeObject(o)
  )

  implicit val string: FromConf[String] = FromConf.instance2(_.getString)

  implicit val int: FromConf[Int] = FromConf.instance2(_.getInt)

  implicit val number: FromConf[Number] = FromConf.instance2(_.getNumber)

  implicit val float: FromConf[Float] = number.map(_.floatValue)

  implicit val double: FromConf[Double] = number.map(_.doubleValue)

  implicit val boolean: FromConf[Boolean] = FromConf.instance2(_.getBoolean)

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
