package com.github.scrud.types

import org.scalatest.FunSpec
import java.util.{Calendar, GregorianCalendar}
import org.scalatest.matchers.MustMatchers

/**
 * Created by Eric Pabst.
 */
class DateWithoutTimeQTSpec extends FunSpec with MustMatchers {
  it("must format dates in short format for editing") {
    val string = DateWithoutTimeQT.convertToString(new GregorianCalendar(2020, Calendar.JANUARY, 20).getTime)
    string must not(include ("Jan"))
    string must include ("1")
  }

  it("must format dates in short format") {
    val string = DateWithoutTimeQT.convertToDisplayString(new GregorianCalendar(2020, Calendar.JANUARY, 20).getTime)
    string must include ("Jan")
  }
}
