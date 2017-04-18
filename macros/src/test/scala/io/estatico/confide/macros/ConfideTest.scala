package io.estatico.confide.macros

import io.estatico.confide.ConfideFactory
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.PropertyChecks

import scala.language.reflectiveCalls

class ConfideTest extends PropSpec with PropertyChecks with Matchers {

  property("@Conf macro") {

    @Conf case class Meal(breakfast: Breakfast)
    @Conf case class Breakfast(
      name: String,
      spam: Spam,
      eggs: List[Egg]
    )
    @Conf case class Spam(slices: Int)
    @Conf case class Egg(scrambled: Boolean)

    val config = ConfideFactory.parseString[Meal]("""
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

    config shouldEqual Meal(Breakfast(
      name = "monty",
      spam = Spam(slices = 4),
      eggs = List(
        Egg(scrambled = true),
        Egg(scrambled = false)
      )
    ))
  }
}
