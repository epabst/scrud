package com.github.scrud.types

import org.scalatest.{MustMatchers, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A behavior specification for [[com.github.scrud.types.IntQualifiedType]].
 */
@RunWith(classOf[JUnitRunner])
class IntQualifiedTypeSpec extends FunSpec with MustMatchers {
  describe("convertFromEditString") {
    it("must handle an unparseable string") {
      val qualifiedType = new IntQualifiedType {}
      qualifiedType.convertFromString("blah blah").isFailure must be (true)
    }
  }
}
