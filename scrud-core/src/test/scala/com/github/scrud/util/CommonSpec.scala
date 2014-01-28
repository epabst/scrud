package com.github.scrud.util

//import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec

/** A behavior specification for [[com.github.scrud.util.Common]].
  * @author Eric Pabst (epabst@gmail.com)
  */

//@RunWith(classOf[JUnitRunner])
class CommonSpec extends FunSpec with MustMatchers {
  describe("tryToEvaluate") {
    it("must evaluate and return the parameter") {
      Common.tryToEvaluate("hello" + " world") must be (Some("hello world"))
    }

    it("must return None if an exception occurs") {
      Common.tryToEvaluate(sys.error("intentional")) must be (None)
    }
  }
}
