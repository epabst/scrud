package com.github.scrud.types

import org.scalatest.{MustMatchers, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A specification for [[com.github.scrud.types.EnumerationValueQT]].
 * Created by eric on 5/13/14.
 */
@RunWith(classOf[JUnitRunner])
class EnumerationValueQTSpec extends FunSpec with MustMatchers {
  object MyEnum extends Enumeration {
    val A = Value("A")
    val B = Value("B")
  }
  val format = EnumerationValueQT[MyEnum.Value](MyEnum)

  it("must handle formatting/parsing") {
    format.convertToString(MyEnum.A) must be ("A")
    format.convertToString(MyEnum.B) must be ("B")
    itMustFormatAndParse(format, MyEnum.A)
    itMustFormatAndParse(format, MyEnum.B)
    format.convertFromString("").isSuccess must be (false)
  }

  def itMustFormatAndParse[T](qualifiedType: StringConvertibleQT[T], value: T) {
    val string = qualifiedType.convertToString(value)
    qualifiedType.convertFromString(string).get must be (value)
  }
}
