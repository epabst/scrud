package com.github.scrud.copy.types

import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import Validation._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/** A behavior specification for [[Validation]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class ValidationSpec extends FunSpec with MustMatchers {
  describe("required") {
    val requiredInt = required[Int]

    it("must detect an empty value") {
      requiredInt.isValid(None) must be (false)
    }

    it("must accept a defined value") {
      requiredInt.isValid(Some(0)) must be (true)
    }
  }

  describe("requiredAndNot") {
    val validation = requiredAndNot[Int](1, 3)

    it("must detect an empty value") {
      validation.isValid(None) must be (false)
    }

    it("must detect a matching (non-empty) value and consider it invalid") {
      validation.isValid(Some(3)) must be (false)
    }

    it("must detect a non-matching (non-empty) value and consider it valid") {
      validation.isValid(Some(2)) must be (true)
    }
  }

  describe("requiredString") {
    it("must detect an empty value") {
      requiredString.isValid(None) must be (false)
    }

    it("must consider an empty string as invalid") {
      requiredString.isValid(Some("")) must be (false)
    }

    it("must consider an string with just whitespace as invalid") {
      requiredString.isValid(Some(" \t\r\n ")) must be (false)
    }

    it("must consider a non-empty string as valid") {
      requiredString.isValid(Some("hello")) must be (true)
    }
  }
}
