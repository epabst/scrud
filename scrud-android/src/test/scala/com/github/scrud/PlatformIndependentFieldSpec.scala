package com.github.scrud

import com.github.triangle.PortableField._
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A specification for [[com.github.scrud.PlatformIndependentField]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:38 PM
 */
@RunWith(classOf[JUnitRunner])
class PlatformIndependentFieldSpec extends FunSpec with MustMatchers {
  describe("loadingIndicator") {
    it("must return a value") {
      val stringField = mapField[String]("name") + PlatformIndependentField.loadingIndicator("...")
      val loadingValue = stringField.copyFrom(LoadingIndicator)
      loadingValue.update(Map.empty[String,String]) must be (Map("name" -> "..."))
    }
  }
}
