package com.github.scrud

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import Validation._
import com.github.triangle.UpdaterInput

/** A behavior specification for [[com.github.scrud.Validation]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class ValidationSpec extends FunSpec with MustMatchers {
  describe("required") {
    val requiredInt = required[Int]

    it("must detect an empty value") {
      requiredInt.updateWithValue(ValidationResult.Valid, None) must be (ValidationResult(1))
    }

    it("must accept a defined value") {
      requiredInt.updateWithValue(ValidationResult.Valid, Some(0)) must be (ValidationResult.Valid)
    }
  }

  describe("requiredAndNot") {
    val updater = requiredAndNot[Int](1, 3).updater[ValidationResult]

    it("must detect an empty value") {
      updater(UpdaterInput(ValidationResult.Valid, None)) must be (ValidationResult(1))
    }

    it("must detect a matching (non-empty) value and consider it invalid") {
      updater(UpdaterInput(ValidationResult.Valid, Some(3))) must be (ValidationResult(1))
    }

    it("must detect a non-matching (non-empty) value and consider it valid") {
      updater(UpdaterInput(ValidationResult.Valid, Some(2))) must be (ValidationResult.Valid)
    }
  }

  describe("requiredString") {
    it("must detect an empty value") {
      requiredString.updateWithValue(ValidationResult.Valid, None) must be (ValidationResult(1))
    }

    it("must consider an empty string as invalid") {
      requiredString.updateWithValue(ValidationResult.Valid, Some("")) must be (ValidationResult(1))
    }

    it("must consider an string with just whitespace as invalid") {
      requiredString.updateWithValue(ValidationResult.Valid, Some(" \t\r\n ")) must be (ValidationResult(1))
    }

    it("must consider a non-empty string as valid") {
      requiredString.updateWithValue(ValidationResult.Valid, Some("hello")) must be (ValidationResult.Valid)
    }
  }
}
