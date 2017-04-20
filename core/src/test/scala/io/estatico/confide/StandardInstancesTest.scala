package io.estatico.confide

import org.scalacheck.Arbitrary
import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.words.ResultOfStringPassedToVerb

import scala.concurrent.duration._

class StandardInstancesTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  deriveTest[Int]
  deriveTest[Float]
  deriveTestWith[Number](n => BigDecimal(n.toString))
  deriveTest[Double]
  deriveTest[Boolean]

  testWith[String] in {
    assert(decode[String](""" "" """) == "")
    assert(decode[String]("foo") == "foo")
    assert(decode[String](""" "bar \"baz\" quux" """) == """bar "baz" quux""")
  }

  testWith[FiniteDuration] in {
    assert(decode[FiniteDuration]("1s") == 1.second)
    assert(decode[FiniteDuration]("30ms") == 30.millis)
    assert(decode[FiniteDuration]("-2h") == (-2).hours)
  }

  private def testWith[A : Manifest]: ResultOfStringPassedToVerb = {
    val man = implicitly[Manifest[A]]
    s"FromConf[$man]" should s"decode $man values"
  }

  /**
   * For values that can be config encoded with .toString, we can use `generateTest`.
   * We then use `f` to convert the value into something that can be compared.
   */
  private def deriveTestWith[A : FromConf : Manifest : Arbitrary](f: A => Any): Unit = {
    testWith[A] in forAll { a: A =>
      assert(f(decode[A](a.toString)) == f(a))
    }
  }

  /** Same as `deriveTestWith` except compares values directly. */
  //noinspection UnitMethodIsParameterless
  private def deriveTest[A : FromConf : Manifest : Arbitrary]: Unit = deriveTestWith[A](identity)

  private def decode[A : FromConf](value: String): A = {
    FromConf[A].get(ConfideFactory.raw.parseString(s"x=$value"), "x")
  }
}
