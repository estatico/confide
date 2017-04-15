package io.estatico.confide

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import com.typesafe.config.ConfigFactory

import scala.language.reflectiveCalls

class ConfideTest extends PropSpec with PropertyChecks with Matchers {

  property("get macro in simple object") {
    val parsed = ConfigFactory.parseString("""
      foo = 1
      bar = baz
      quux = [spam, eggs]
    """)

    val c = new RelConf {
      val config = parsed
      val foo = get[Int]
      val bar = get[String]
      val quux = get[Vector[String]]
    }

    c.foo shouldEqual 1
    c.bar shouldEqual "baz"
    c.quux shouldEqual Vector("spam", "eggs")
  }

  property("get macro in nested object") {
    val parsed = ConfigFactory.parseString("""
      foo {
        bar = baz
        quux.spam = 4
      }
    """)

    val c = new RelConf {
      val config = parsed
      val foo = new SubConf(__) {
        val bar = get[String]
        val quux = new SubConf(__) {
          val spam = get[Int]
        }
      }
    }

    c.foo.bar shouldEqual "baz"
    c.foo.quux.spam shouldEqual 4
  }

  property("get macro in trait") {
    val parsed = ConfigFactory.parseString("""
      foo {
        size = 1
        message = "hey"
        things = [1.2, 3.4]
      }
    """)

    trait Foo {
      def size: Int
      def message: String
      def things: Vector[Float]
    }

    val c = new RelConf {
      val config = parsed
      val foo = new SubConf(__) with Foo {
        val size = get[Int]
        val message = get[String]
        val things = get[Vector[Float]]
      }
    }

    c.foo.size shouldEqual 1
    c.foo.message shouldEqual "hey"
    c.foo.things shouldEqual Vector(1.2f, 3.4f)
  }

  property("@Conf macro") {
    val parsed = ConfigFactory.parseString("""
      breakfast {
        name: monty
        spam {
          slices: 4
        }
        eggs: [
          { scrambled: true },
          { scrambled: false }
        ]
      }
    """)

    @Conf case class Breakfast(
      name: String,
      spam: Spam,
      eggs: List[Egg]
    )
    @Conf case class Spam(slices: Int)
    @Conf case class Egg(scrambled: Boolean)

    val c = new RelConf {
      val config = parsed
      val breakfast = get[Breakfast]
    }

    c.breakfast shouldEqual Breakfast(
      name = "monty",
      spam = Spam(slices = 4),
      eggs = List(
        Egg(scrambled = true),
        Egg(scrambled = false)
      )
    )
  }
}
