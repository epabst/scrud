package com.github.scrud.types

import org.scalatest.{MustMatchers, FunSpec}

/**
 * A behavior specification for [[com.github.scrud.types.IntQualifiedType]].
 */
class IntQualifiedTypeSpec extends FunSpec with MustMatchers {
  describe("convertFromEditString") {
    it("must handle an unparseable string") {
      val qualifiedType = new IntQualifiedType {}
      qualifiedType.convertFromString("blah blah").isFailure must be (true)
    }
  }
}
