package com.github.scrud.context

import org.scalatest.{MustMatchers, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Behavior specification for [[com.github.scrud.context.ApplicationName]].
 */
@RunWith(classOf[JUnitRunner])
class ApplicationNameSpec extends FunSpec with MustMatchers {
  it("must have a snake-case logTag") {
    val applicationName = ApplicationName("A Difficult name to use as an ID")
    applicationName.logTag must be ("a_difficult_name_to_use_as_an_id")
  }
}
