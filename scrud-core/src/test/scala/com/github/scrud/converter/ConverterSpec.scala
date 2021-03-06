package com.github.scrud.converter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import Converter._
import GenericConverter._
import java.util.{Calendar, GregorianCalendar, Date}
import java.text.DateFormat
import scala.concurrent.ops
import scala.util.Success

/** A behavior specification for [[com.github.scrud.converter.Converter]].
  * @author Eric Pabst (epabst@gmail.com)
  */

@RunWith(classOf[JUnitRunner])
class ConverterSpec extends FunSpec with MustMatchers {
  describe("identityConverter") {
    it("must be equal, regardless of its type") {
      val converter1 = identityConverter[String]
      val converter2 = identityConverter[Date]
      converter1 must be (converter2)
    }
  }

  describe("anyToString") {
    it("must use the Object.toString method") {
      val from = new Object
      anyToString.convert(from) must be (Success(from.toString))
    }

    it("must work for primitive types") {
      anyToString.convert(1) must be (Success("1"))
      anyToString.convert(1.5) must be (Success("1.5"))
      anyToString.convert(true) must be (Success("true"))
      anyToString.convert('a') must be (Success("a"))
    }
  }

  describe("stringToAnyVal") {
    it("must convert between primitive types") {
      stringToAnyVal.convertTo[Int]("1") must be (Success(1))
      stringToAnyVal.convertTo[Long]("123") must be (Success(123L))
      stringToAnyVal.convertTo[Int]("123") must be (Success(123))
      stringToAnyVal.convertTo[Short]("123") must be (Success(123))
      stringToAnyVal.convertTo[Byte]("123") must be (Success(123))
      stringToAnyVal.convertTo[Double]("3232.11") must be (Success(3232.11))
      stringToAnyVal.convertTo[Float]("2.3") must be (Success(2.3f))
      stringToAnyVal.convertTo[Boolean]("true") must be (Success(true))
    }

    it("must return Failure if unable to parse") {
      stringToAnyVal.convertTo[Int]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Long]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Int]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Short]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Byte]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Double]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Float]("foo").isSuccess must be (false)
      stringToAnyVal.convertTo[Boolean]("foo").isSuccess must be (false)
    }
  }

  describe("stringToDate") {
    it("must parse various date formats") {
      stringToDate.convert("1/19/2005").get must be(new GregorianCalendar(2005, Calendar.JANUARY, 19).getTime)
      stringToDate.convert("12/1/2005").get must be(new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime)
      stringToDate.convert("1 Dec 2005").get must be(new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime)
      stringToDate.convert("2013-6-6").get must be(new GregorianCalendar(2013, Calendar.JUNE, 6).getTime)
    }

    it("must be thread-safe") {
      List(ops.future {
        (1 to 1000).foreach { _ =>
         stringToDate.convert("1/19/2005").get must be(new GregorianCalendar(2005, Calendar.JANUARY, 19).getTime)
        }
      }, ops.future {
        (1 to 1000).foreach { _ =>
          stringToDate.convert("12/1/2005").get must be(new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime)
        }
      }, ops.future {
        (1 to 1000).foreach { _ =>
          stringToDate.convert("1 Dec 2005").get must be(new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime)
        }
      }, ops.future {
        (1 to 1000).foreach { _ =>
          stringToDate.convert("2013-6-6").get must be(new GregorianCalendar(2013, Calendar.JUNE, 6).getTime)
        }
      }).foreach(_.apply())
    }

    it("must handle the default format for the current Locale") {
      val date = new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime
      val string = DateFormat.getDateInstance.format(date)
      stringToDate.convert(string).get must be(date)
    }
  }

  describe("dateToString") {
    it("must format a date") {
      dateToString.convert(new Date()).isSuccess must be (true)
    }

    it("must use the 'short' format for the current Locale") {
      val date = new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime
      val expectedString = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
      dateToString.convert(date).get must be(expectedString)
    }
  }

  describe("dateToDisplayString") {
    it("must format a date") {
      dateToDisplayString.convert(new Date()).isSuccess must be (true)
    }

    it("must use the default format for the current Locale") {
      val date = new GregorianCalendar(2005, Calendar.DECEMBER, 1).getTime
      val expectedString = DateFormat.getDateInstance.format(date)
      dateToDisplayString.convert(date).get must be(expectedString)
    }
  }

  describe("stringToCurrency") {
    it("must parse various number formats") {
      stringToCurrency.convert("$1.00").get must be(1.0)
      stringToCurrency.convert("$1").get must be(1.0)
      stringToCurrency.convert("1.00").get must be(1.0)
      stringToCurrency.convert("1").get must be(1.0)
      stringToCurrency.convert("-1.00").get must be(-1.0)
      stringToCurrency.convert("-1").get must be(-1.0)
      stringToCurrency.convert("($1.00)").get must be(-1.0)
      stringToCurrency.convert("($1)").get must be(-1.0)
      //do these later if desired
      stringToCurrency.convert("(1.00)").isSuccess must be (false)
      stringToCurrency.convert("(1)").isSuccess must be (false)
      stringToCurrency.convert("-$1.00").isSuccess must be (false)
      stringToCurrency.convert("-$1").isSuccess must be (false)
    }
  }

  describe("currencyToString") {
    it("must format correctly") {
      currencyToString.convert(1234.2) must be (Success("1,234.20"))
      currencyToString.convert(1234.22324) must be (Success("1,234.22"))
    }
  }

  describe("currencyToEditString") {
    it("must format correctly") {
      currencyToEditString.convert(1234.2) must be (Success("1,234.20"))
      currencyToEditString.convert(1234.22324) must be (Success("1,234.22"))
    }
  }

  describe("stringToPercentage") {
    it("must parse various number formats") {
      stringToPercentage.convert("5.00%").get must be(0.05f)
      stringToPercentage.convert("1%").get must be(0.01f)
      stringToPercentage.convert("1.00").get must be(0.01f)
      stringToPercentage.convert("1").get must be(0.01f)
      stringToPercentage.convert("-1.00").get must be(-0.01f)
      stringToPercentage.convert("-1").get must be(-0.01f)
      stringToPercentage.convert("100%").get must be(1.0f)
    }
  }

  describe("percentageToString") {
    it("must format correctly") {
      percentageToString.convert(0.1234f) must be (Success("12%"))
      percentageToString.convert(0.20f) must be (Success("20%"))
    }
  }

  describe("percentageToEditString") {
    it("must format correctly") {
      percentageToEditString.convert(0.1234f) must be (Success("12"))
      percentageToEditString.convert(0.20f) must be (Success("20"))
    }
  }

  describe("stringToEnum") {
    object MyEnum extends Enumeration {
      val A = Value("A")
      val B = Value("B")
    }
    val converter = stringToEnum[MyEnum.Value](MyEnum)

    it("must convert") {
      converter.convert("A") must be (Success(MyEnum.A))
      converter.convert("B") must be (Success(MyEnum.B))
    }

    it("must return None if unable to convert") {
      converter.convert("C").isSuccess must be (false)
      converter.convert("").isSuccess must be (false)
    }
  }
}
