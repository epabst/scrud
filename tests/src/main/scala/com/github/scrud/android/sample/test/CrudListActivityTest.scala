package com.github.scrud.android.sample.test

import junit.framework.Assert._
import android.test.{TouchUtils, ActivityInstrumentationTestCase2}
import com.github.scrud.android.{CrudListActivity, CrudActivity, sample}
import sample.{SampleApplication, AuthorEntityType}
import android.widget.TextView
import android.view.Menu
import android.app.Activity
import com.github.scrud.android.action.Action
import com.github.scrud.android.common.Timing._
import com.github.scrud.android.common.Timing

class CrudListActivityTest extends ActivityInstrumentationTestCase2(classOf[CrudListActivity]) {
  val instrumentation = getInstrumentation

  def testEmptyList() {
    val activity = getActivity
    val application = activity.crudApplication
    assertEquals(activity.entityType, AuthorEntityType)
    assertEquals(classOf[SampleApplication], application.getClass)

    instrumentation.callActivityOnCreate(activity, null)
    activity.waitForWorkInProgress()
    instrumentation.waitForIdleSync()

    instrumentation.addMonitor(classOf[CrudActivity].getCanonicalName, )
    invokeOptionMenuAction(application.actionToCreate(AuthorEntityType).get, activity)
    instrumentation.waitForIdleSync()

    instrumentation.runOnMainSync(toRunnable {
      AuthorEntityType.copyAndUpdate(Map("name" -> "George"), activity)
    })
    activity.waitForWorkInProgress()

//    TouchUtils.tapView()

    instrumentation.runOnMainSync(toRunnable {
      val dataInActivity = AuthorEntityType.copyAndUpdate(activity, Map.empty[String, Any])
      //todo assertEquals(Map("name" -> "George"), dataInActivity)
      assertEquals(Map.empty, dataInActivity)
    }

    val textview = activity.findViewById(sample.R.id.name).asInstanceOf[TextView]
    assertEquals(textview.getText, "")
  }

  def invokeOptionMenuAction(action: Action, activity: CrudListActivity) {
    val command = action.command
    val result = instrumentation.invokeMenuActionSync(activity, activity.optionsMenuCommands.indexOf(command), 0)
    assertTrue("invokeMenuActionSync should return true", result)
  }
}
