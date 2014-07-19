package com.github.scrud

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A specification for [[com.github.scrud.LoadingIndicator]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:38 PM
 */
@RunWith(classOf[JUnitRunner])
class LoadingIndicatorSpec extends FunSpec with MustMatchers {
  it("must return a value") {
    val loadingIndicator = LoadingIndicator("...")
    val loadingValueOpt = loadingIndicator.toAdaptableField.findSourceField(LoadingIndicator).flatMap(_.findValue(LoadingIndicator, null))
    loadingValueOpt must be (Some("..."))
  }
}
