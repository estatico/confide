package io.estatico.confide

import org.scalatest._

class ConfMacroTest extends FlatSpec with Matchers {

  "@Conf" should "work without generics" in {
    @Conf case class Meal(breakfast: Breakfast)
    @Conf case class Breakfast(name: String, spam: Spam, eggs: List[Egg])
    @Conf case class Spam(slices: Int)
    @Conf case class Egg(scrambled: Boolean)

    ConfideFactory.parseString[Meal]("""
      breakfast {
        name: monty
        spam { slices: 4 }
        eggs: [
          { scrambled: true },
          { scrambled: false }
        ]
      }
    """) shouldEqual Meal(Breakfast(
      name = "monty",
      spam = Spam(slices = 4),
      eggs = List(
        Egg(scrambled = true),
        Egg(scrambled = false)
      )
    ))
  }

  it should "work with type params" in {
    trait Meal
    @Conf case class Breakfast(breakfast: String) extends Meal
    @Conf case class Lunch(lunch: String) extends Meal
    @Conf case class Root[M <: Meal](meal: M)

    ConfideFactory.parseString[Root[Breakfast]]("""
      meal { breakfast=british }
    """) shouldEqual Root(Breakfast("british"))

    ConfideFactory.parseString[Root[Lunch]]("""
      meal { lunch=indian }
    """) shouldEqual Root(Lunch("indian"))
  }

  it should "work for non-object type params" in {
    @Conf case class Foo[A](a: A)
    ConfideFactory.parseString[Foo[String]]("""
      a=bar
    """) shouldEqual Foo("bar")
  }
}
