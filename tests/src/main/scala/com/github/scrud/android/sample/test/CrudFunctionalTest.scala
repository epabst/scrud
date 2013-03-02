package com.github.scrud.android.sample.test

import junit.framework.Assert._
import android.test.ActivityInstrumentationTestCase2
import com.github.scrud.android.{PersistenceDeniedInUIThread, LastException, CrudActivity, sample}
import sample._
import com.jayway.android.robotium.solo.Solo
import com.github.triangle.PortableValue
import com.github.scrud.{EntityName, EntityType}
import android.app.Instrumentation
import com.github.scrud.action.CrudOperationType
import com.github.scrud.util.Common
import com.github.scrud.action.CrudOperation
import scala.Some

class CrudFunctionalTest extends ActivityInstrumentationTestCase2(classOf[CrudActivity]) {
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

  // not a val since dynamic
  def currentCrudActivity: CrudActivity = solo.getCurrentActivity.asInstanceOf[CrudActivity]

  def testAddEditDelete_PersistenceNotAllowedInUIThread() {
    PersistenceDeniedInUIThread.set(currentCrudActivity.crudContext, true)
    try {
      testAddEditDelete()
    } finally {
      PersistenceDeniedInUIThread.clear(currentCrudActivity.crudContext)
    }
  }

  def testAddEditDelete() {
    val crudContext = currentCrudActivity.crudContext
    try {
      val application = currentCrudActivity.crudApplication.asInstanceOf[SampleApplication]
      import application._

      assertEquals(CrudOperation(Author, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.clickOnMenuItem("Add Author")
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Author, CrudOperationType.Create), currentCrudActivity.currentCrudOperation)

      copyToCurrentActivity(authorEntityType.copyFrom(Map("name" -> Some("Orson Scott Card"))))

      solo.goBack()
      solo.waitForText("Saved", 1, 5000)
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Author, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.clickOnText("Orson Scott Card")
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Book, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.clickOnMenuItem("Add Book")
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Book, CrudOperationType.Create), currentCrudActivity.currentCrudOperation)

      solo.enterText(0, "Ender's Game")
      val bookData = copyFromCurrentActivity(Book).update(Map.empty[String, Option[Any]])
      assertEquals(Some("Ender's Game"), bookData.apply("name"))
      assertTrue("There should be a default Genre", bookData.apply("genre").isDefined)

      solo.goBack()
      solo.waitForText("Saved", 1, 5000)
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Book, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.goBack()
      solo.waitForText("Saved", 1, 5000)
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Author, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.clickLongOnText("Orson Scott Card")
      solo.clickOnText("Edit Author")
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Author, CrudOperationType.Update), currentCrudActivity.currentCrudOperation)
      assertEquals(Some("Orson Scott Card"), copyFromCurrentActivity(Author).update(Map.empty[String,Option[Any]]).apply("name"))

      solo.clearEditText(0)
      solo.enterText(0, "Mark Twain")
      assertEquals(Some("Mark Twain"), copyFromCurrentActivity(Author).update(Map.empty[String,Option[Any]]).apply("name"))

      solo.goBack()
      solo.waitForText("Saved", 1, 5000)
      solo.waitForActivity(classOf[CrudActivity].getSimpleName)
      assertEquals(CrudOperation(Author, CrudOperationType.List), currentCrudActivity.currentCrudOperation)

      solo.clickLongOnText("Mark Twain")
      solo.clickOnText("Delete")
      currentCrudActivity.waitForWorkInProgress()
      instrumentation.waitForIdleSync()
      assertEquals(CrudOperation(Author, CrudOperationType.List), currentCrudActivity.currentCrudOperation)
    } catch {
      case e: Throwable =>
        LastException.get(crudContext).foreach { lastException =>
          throw lastException
//          lastException.printStackTrace(System.err)
//          e.initCause(lastException)
        }
        throw e
    }
  }

  def copyToCurrentActivity(portableValue: PortableValue) {
    val currentCrudActivity = this.currentCrudActivity
    instrumentation.runOnMainSync(Common.toRunnable {
      portableValue.update(currentCrudActivity)
    })
  }

  def copyFromCurrentActivity(entityName: EntityName): PortableValue = {
    copyFromCurrentActivity(currentCrudActivity.crudApplication.entityType(entityName))
  }

  def copyFromCurrentActivity(entityType: EntityType): PortableValue = {
    var result: Option[PortableValue] = None
    instrumentation.runOnMainSync(Common.toRunnable {
      result = Some(entityType.copyFrom(currentCrudActivity))
    })
    result.get
  }
}
