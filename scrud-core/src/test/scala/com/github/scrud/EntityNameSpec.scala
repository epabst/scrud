package com.github.scrud

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers

/**
 * A behavior specification for [[com.github.scrud.EntityName]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/4/13
 *         Time: 8:56 AM
 */
class EntityNameSpec extends FunSpec with MustMatchers {
  describe("toDisplayableString") {
    it("must not add spaces for a single word") {
      EntityName("Book").toDisplayableString must be ("Book")
    }

    it("must put spaces between words") {
      EntityName("FastJumpingMonkey").toDisplayableString must be ("Fast Jumping Monkey")
    }
  }
}
