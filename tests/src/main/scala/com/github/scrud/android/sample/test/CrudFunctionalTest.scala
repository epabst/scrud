package com.github.scrud.android.sample.test

import junit.framework.Assert._
import android.test.ActivityInstrumentationTestCase2
import com.github.scrud.android.{BaseCrudActivity, CrudListActivity, CrudActivity, sample}
import sample.{BookEntityType, SampleApplication, AuthorEntityType}
import com.jayway.android.robotium.solo.Solo
import com.github.scrud.android.action.Operation
import com.github.scrud.android.common.Timing._
import com.github.triangle.PortableValue
import com.github.scrud.android.persistence.EntityType
import android.app.Instrumentation
import android.content.Intent

class CrudFunctionalTest extends ActivityInstrumentationTestCase2(classOf[CrudListActivity]) {
  var instrumentation: Instrumentation = _
  var solo: Solo = _

  override def setUp() {
    super.setUp()
    instrumentation = getInstrumentation
    this.solo = new Solo(instrumentation, getActivity)
  }

  override def tearDown() {
    solo.finishOpenedActivities()
    super.tearDown()
  }

  def currentCrudActivity: BaseCrudActivity = solo.getCurrentActivity.asInstanceOf[BaseCrudActivity]

  def testAddEditDelete() {
    assertEquals(classOf[SampleApplication], currentCrudActivity.crudApplication.getClass)
    assertEquals(currentCrudActivity.entityType, AuthorEntityType)
    // This would normally be Operation.ListActionName, but it is the starting intent.
    assertEquals(Intent.ACTION_MAIN, currentCrudActivity.currentAction)

    solo.clickOnMenuItem("Add Author")
    solo.waitForActivity(classOf[CrudActivity].getSimpleName)
    assertEquals(AuthorEntityType, currentCrudActivity.entityType)
    assertEquals(Operation.CreateActionName, currentCrudActivity.currentAction)

    copyToCurrentActivity(AuthorEntityType.copyFrom(Map("name" -> "Orson Scott Card")))

    solo.goBack()
    solo.waitForText("Saved", 1, 5000)
    solo.waitForActivity(classOf[CrudListActivity].getSimpleName)
    assertEquals(AuthorEntityType, currentCrudActivity.entityType)
    // This would normally be Operation.ListActionName, but it is the starting intent.
    assertEquals(Intent.ACTION_MAIN, currentCrudActivity.currentAction)

    solo.clickOnText("Orson Scott Card")
    solo.waitForActivity(classOf[CrudListActivity].getSimpleName)
    assertEquals(BookEntityType, currentCrudActivity.entityType)
    assertEquals(Operation.ListActionName, currentCrudActivity.currentAction)

    solo.clickOnMenuItem("Add Book")
    solo.waitForActivity(classOf[CrudActivity].getSimpleName)
    assertEquals(BookEntityType, currentCrudActivity.entityType)
    assertEquals(Operation.CreateActionName, currentCrudActivity.currentAction)

    solo.enterText(0, "Ender's Game")
    val bookData = copyFromCurrentActivity(BookEntityType).update(Map.empty[String, Any])
    assertEquals(Some("Ender's Game"), bookData.get("name"))
    assertTrue("There should be a default Genre", bookData.get("genre").isDefined)

    solo.goBack()
    solo.waitForText("Saved", 1, 5000)
    solo.waitForActivity(classOf[CrudListActivity].getSimpleName)
    assertEquals(BookEntityType, currentCrudActivity.entityType)
    assertEquals(Operation.ListActionName, currentCrudActivity.currentAction)

    solo.goBack()
    solo.waitForText("Saved", 1, 5000)
    solo.waitForActivity(classOf[CrudListActivity].getSimpleName)
    assertEquals(AuthorEntityType, currentCrudActivity.entityType)
    // This would normally be Operation.ListActionName, but it is the starting intent.
    assertEquals(Intent.ACTION_MAIN, currentCrudActivity.currentAction)

    solo.clickLongOnText("Orson Scott Card")
    solo.clickOnText("Edit Author")
    solo.waitForActivity(classOf[CrudActivity].getSimpleName)
    assertEquals(AuthorEntityType, currentCrudActivity.entityType)
    assertEquals(Operation.UpdateActionName, currentCrudActivity.currentAction)
    assertEquals(Some("Orson Scott Card"), copyFromCurrentActivity(AuthorEntityType).update(Map.empty[String,Any]).get("name"))

    solo.clearEditText(0)
    solo.enterText(0, "Mark Twain")
    assertEquals(Some("Mark Twain"), copyFromCurrentActivity(AuthorEntityType).update(Map.empty[String,Any]).get("name"))

    solo.goBack()
    solo.waitForText("Saved", 1, 5000)
    solo.waitForActivity(classOf[CrudListActivity].getSimpleName)
    assertEquals(AuthorEntityType, currentCrudActivity.entityType)
    // This would normally be Operation.ListActionName, but it is the starting intent.
    assertEquals(Intent.ACTION_MAIN, currentCrudActivity.currentAction)

    solo.clickLongOnText("Mark Twain")
    solo.clickOnText("Delete")
    currentCrudActivity.waitForWorkInProgress()
    instrumentation.waitForIdleSync()
  }


  def copyToCurrentActivity(portableValue: PortableValue) {
    val currentCrudActivity = this.currentCrudActivity
    instrumentation.runOnMainSync(toRunnable {
      portableValue.update(currentCrudActivity)
    })
  }

  def copyFromCurrentActivity(entityType: EntityType): PortableValue = {
    var result: Option[PortableValue] = None
    instrumentation.runOnMainSync(toRunnable {
      result = Some(entityType.copyFrom(currentCrudActivity))
    })
    result.get
  }
}
