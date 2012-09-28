package com.github.scrud.android.sample.test

import junit.framework.Assert._
import _root_.android.test.ActivityInstrumentationTestCase2
import com.github.scrud.android.CrudActivity
import com.github.scrud.android.sample
import sample.AuthorEntityType
import android.widget.TextView

class CrudActivityTest extends ActivityInstrumentationTestCase2(classOf[CrudActivity]) {
  def testHelloWorldIsShown() {
    val activity = getActivity
    assertEquals(activity.entityType, AuthorEntityType)

    activity.onCreate(savedInstanceState = null)
    activity.waitForWorkInProgress()

    AuthorEntityType.copyAndUpdate(Map("name" -> "George"), activity)
    activity.waitForWorkInProgress()

    val dataInActivity = AuthorEntityType.copyAndUpdate(activity, Map.empty[String, Any])
    //todo assertEquals(Map("name" -> "George"), dataInActivity)
    assertEquals(Map.empty, dataInActivity)

    val textview = activity.findViewById(sample.R.id.name).asInstanceOf[TextView]
    assertEquals(textview.getText, "")
  }
}
