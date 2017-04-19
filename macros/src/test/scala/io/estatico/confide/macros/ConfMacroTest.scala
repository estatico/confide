package io.estatico.confide
package macros

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.PropertyChecks

import scala.language.reflectiveCalls

class ConfMacroTest extends PropSpec with PropertyChecks with Matchers {

  property("@Conf macro without generics") {

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

  property("@Conf macro with generics") {

    @Conf case class Root[M <: Meal](meal: M)

    trait Meal
    @Conf case class Breakfast(breakfast: String) extends Meal
    @Conf case class Lunch(lunch: String) extends Meal

    val breakfast = ConfideFactory.parseString[Root[Breakfast]]("""
      meal { breakfast=british }
    """)

    breakfast shouldEqual Root(Breakfast("british"))

    val lunch = ConfideFactory.parseString[Root[Lunch]]("""
      meal { lunch=indian }
    """)

    lunch shouldEqual Root(Lunch("indian"))
  }
}
