package com.github.scrud.android.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase
import _root_.android.test.ActivityInstrumentationTestCase2
import com.github.scrud.android.CrudActivity
import com.github.scrud.android.sample.{TypedResource, TR}
import TypedResource._

class AndroidTests extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("com.github.scrud.android", getContext.getPackageName)
  }
}

class ActivityTests extends ActivityInstrumentationTestCase2(classOf[CrudActivity]) {
   def testHelloWorldIsShown() {
      val activity = getActivity
      val textview = activity.findView(TR.edition)
      assertEquals(textview.getText, "Edition")
    }
}
