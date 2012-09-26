package com.github.scrud.android.sample.test

import junit.framework.Assert._
import _root_.android.test.ActivityInstrumentationTestCase2
import com.github.scrud.android.CrudActivity
import com.github.scrud.android.tests.{TypedResource, TR}
import TypedResource._

class CrudActivityTest extends ActivityInstrumentationTestCase2(classOf[CrudActivity]) {
  def testHelloWorldIsShown() {
    val activity = getActivity
    val textview = activity.findView(TR.edition)
    assertEquals(textview.getText, "Edition")
  }
}
