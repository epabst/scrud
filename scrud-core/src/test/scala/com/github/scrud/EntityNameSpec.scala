package com.github.scrud

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * A behavior specification for [[com.github.scrud.EntityName]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/4/13
 *         Time: 8:56 AM
 */
@RunWith(classOf[JUnitRunner])
class EntityNameSpec extends FunSpec with MustMatchers {
  describe("toDisplayableString") {
    it("must not add spaces for a single word") {
      EntityName("Book").toDisplayableString must be ("Book")
    }

    it("must put spaces between words") {
      EntityName("FastJumpingMonkey").toDisplayableString must be ("Fast Jumping Monkey")
    }

    it("must preserve the original") {
      EntityName("My dog's bark is awesome!").toDisplayableString must be ("My dog's bark is awesome!")
    }
  }

  describe("toTitleCase") {
    it("must preserve something that is already correct") {
      EntityName("FastJumpingMonkey").toTitleCase must be ("FastJumpingMonkey")
    }

    it("must remove any spaces that are present and capitalize correctly") {
      EntityName("\t fast Jumping  monkey").toTitleCase must be ("FastJumpingMonkey")
    }

    it("must handle hyphens") {
      EntityName("Pseudo-home").toTitleCase must be ("PseudoHome")
    }

    it("must ignore apostrophes") {
      EntityName("My dog's tail is awesome!").toTitleCase must be ("MyDogsTailIsAwesome")
    }
  }

  describe("toCamelCase") {
    it("must preserve something that is already correct") {
      EntityName("fastJumpingMonkey").toCamelCase must be ("fastJumpingMonkey")
    }

    it("must remove any whitespace that is present and capitalize correctly") {
      EntityName("Fast   jumping \tMonkey").toCamelCase must be ("fastJumpingMonkey")
    }
  }

  describe("toSnakeCase") {
    it("must preserve something that is already correct") {
      EntityName("fast_jumping_monkey").toSnakeCase must be ("fast_jumping_monkey")
    }

    it("must remove any whitespace that is present and format correctly") {
      EntityName("Fast   jumping \tMonkey").toSnakeCase must be ("fast_jumping_monkey")
    }
  }
}
