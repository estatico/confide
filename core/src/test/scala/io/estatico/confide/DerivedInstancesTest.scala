package io.estatico.confide

import org.scalatest.FlatSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

//noinspection NameBooleanParameters
class DerivedInstancesTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  import ConfideFactory.parseString

  "FromConfObj.derive" should "derive instances for case classes" in {
    case class Foo(i: Int, b: Boolean, s: String)
    implicit val fromConfFoo = FromConfObj.derive[Foo]
    val decoded = parseString[Foo]("""
      b=true
      s=yo
      i=1
    """)
    assert(decoded == Foo(1, true, "yo"))
  }

  it should "derive instances for nested case classes" in {
    case class Foo(i: Int, b: Boolean, s: String)
    case class Bar(f: Float, d: Double)
    case class Baz(foo: Foo, bar: Bar)
    implicit val fromConfFoo = FromConfObj.derive[Foo]
    implicit val fromConfBar = FromConfObj.derive[Bar]
    implicit val fromConfBaz = FromConfObj.derive[Baz]
    val decoded = parseString[Baz]("""
      bar {
        d=4.73
        f=1.2
      }
      foo {
        i=2
        s=quux
        b=false
      }
    """)
    assert(decoded == Baz(Foo(2, false, "quux"), Bar(1.2f, 4.73)))
  }

  it should "derive instances for case classes with type params" in {
    case class Foo[A](a: A)
    implicit def fromConfFoo[A : FromConf] = FromConfObj.derive[Foo[A]]
    assert(parseString[Foo[String]]("a=yup") == Foo("yup"))
    assert(parseString[Foo[Float]]("a=3.65") == Foo(3.65f))
    assert(parseString[Foo[Foo[Int]]]("a { a=1 }") == Foo(Foo(1)))
  }
}
