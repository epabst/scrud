package com.github.scrud.converter

import ValueFormat._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import java.util.{Calendar, GregorianCalendar}

/** A behavior specification for [[com.github.scrud.converter.ValueFormat]].
  * @author Eric Pabst (epabst@gmail.com)
  */

@RunWith(classOf[JUnitRunner])
class ValueFormatSpec extends FunSpec with MustMatchers {
  describe("basicFormat") {
    it("must convert between basic types") {
      itMustConvertBetweenTypes[Long](123)
      itMustConvertBetweenTypes[Int](123)
      itMustConvertBetweenTypes[Short](123)
      itMustConvertBetweenTypes[Byte](123)
      itMustConvertBetweenTypes[Double](3232.11)
      itMustConvertBetweenTypes[Float](2.3f)
      itMustConvertBetweenTypes[Boolean](true)
    }

    def itMustConvertBetweenTypes[T <: AnyVal](value: T)(implicit m: Manifest[T]) {
      val format = ValueFormat.basicFormat[T]
      itMustFormatAndParse(format, value)
    }
  }

  describe("persistedDateFormat") {
    val format = persistedDateFormat

    it("must parse correctly") {
      format.toValue("2011-02-01").get must be(new GregorianCalendar(2011, Calendar.FEBRUARY, 1).getTime)
    }

    it("must format correctly") {
      format.toString(new GregorianCalendar(2011, Calendar.FEBRUARY, 1).getTime) must be ("2011-02-01")
    }
  }

  describe("currencyValueFormat") {
    val format = currencyValueFormat

    it("must handle parse various number formats") {
      format.toValue("$1.00").get must be(1.0)
      format.toValue("$1").get must be(1.0)
      format.toValue("1.00").get must be(1.0)
      format.toValue("1").get must be(1.0)
      format.toValue("-1.00").get must be(-1.0)
      format.toValue("-1").get must be(-1.0)
      format.toValue("($1.00)").get must be(-1.0)
      format.toValue("($1)").get must be(-1.0)
      //do these later if desired
      format.toValue("(1.00)").isSuccess must be (false)
      format.toValue("(1)").isSuccess must be (false)
      format.toValue("-$1.00").isSuccess must be (false)
      format.toValue("-$1").isSuccess must be (false)
    }

    it("must format correctly (for editting)") {
      format.toString(1) must be("1.00")
      format.toString(1.2) must be("1.20")
      format.toString(1.23) must be("1.23")
      format.toString(1.233) must be("1.23")
      format.toString(1234.2) must be ("1,234.20")
      format.toString(1234.22324) must be ("1,234.22")
    }
  }

  describe("currencyDisplayValueFormat") {
    val format = currencyDisplayValueFormat

    it("must handle parse various number formats") {
      format.toValue("$1.00").get must be(1.0)
      format.toValue("$1").get must be(1.0)
      format.toValue("1.00").get must be(1.0)
      format.toValue("($1.00)").get must be(-1.0)
      format.toValue("($1)").get must be(-1.0)
      //do these later if desired
      format.toValue("(1.00)").isSuccess must be (false)
      format.toValue("-$1.00").isSuccess must be (false)
      format.toValue("-$1").isSuccess must be (false)
    }

    it("must format correctly") {
      format.toString(1234.2) must be ("1,234.20")
      format.toString(1234.22324) must be ("1,234.22")
    }
  }

  describe("enumFormat") {
    object MyEnum extends Enumeration {
      val A = Value("A")
      val B = Value("B")
    }
    val format = enumFormat[MyEnum.Value](MyEnum)

    it("must handle formatting/parsing") {
      format.toString(MyEnum.A) must be ("A")
      format.toString(MyEnum.B) must be ("B")
      itMustFormatAndParse(format, MyEnum.A)
      itMustFormatAndParse(format, MyEnum.B)
      format.toValue("").isSuccess must be (false)
    }
  }

  def itMustFormatAndParse[T](format: ValueFormat[T], value: T) {
    val string = format.toString(value)
    format.toValue(string).get must be (value)
  }
}