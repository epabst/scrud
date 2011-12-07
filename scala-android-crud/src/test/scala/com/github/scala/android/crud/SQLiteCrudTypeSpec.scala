package com.github.scala.android.crud

import action.{ContextVars, ContextWithVars}
import android.provider.BaseColumns
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.RobolectricTestRunner
import org.scalatest.matchers.MustMatchers
import com.github.triangle._
import persistence.CursorField._
import persistence.{CursorStream, SQLiteCriteria}
import PortableField._
import scala.collection._
import mutable.Buffer
import org.mockito.Mockito
import Mockito._
import android.app.Activity
import android.database.{Cursor, DataSetObserver}
import android.widget.{ListView, ListAdapter}

/**
 * A test for {@link SQLiteCrudType}.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/18/11
 * Time: 6:22 PM
 */
@RunWith(classOf[RobolectricTestRunner])
class SQLiteCrudTypeSpec extends MustMatchers with MyEntityTesting with CrudMockitoSugar {
  val runningOnRealAndroid: Boolean = try {
    debug("Seeing if running on Real Android...")
    Class.forName("com.xtremelabs.robolectric.RobolectricTestRunner")
    warn("NOT running on Real Android.")
    false
  } catch {
    case _ => {
      info("Running on Real Android.")
      true
    }
  }
  val unitAsRef = Unit.asInstanceOf[AnyRef]

  object TestEntityType extends SQLiteCrudType {
    def entityName = "Test"
    val valueFields = List(persisted[Int]("age") + default(21))

    def activityClass = classOf[CrudActivity]
    def listActivityClass = classOf[CrudListActivity]
  }

  object TestApplication extends CrudApplication {
    val name = "Test Application"

    def allEntities = List(TestEntityType)
  }
  val application = TestApplication

  @Test
  def shouldUseCorrectColumnNamesForFindAll() {
    val crudContext = mock[CrudContext]
    stub(crudContext.application).toReturn(application)

    val persistence = new SQLiteEntityPersistence(TestEntityType, crudContext)
    persistence.queryFieldNames must contain(BaseColumns._ID)
    persistence.queryFieldNames must contain("age")
  }

  @Test
  def shouldCloseCursorsWhenClosing() {
    val crudContext = mock[CrudContext]
    stub(crudContext.vars).toReturn(new ContextVars {})
    stub(crudContext.application).toReturn(application)

    val cursors = Buffer[Cursor]()
    val persistence = new SQLiteEntityPersistence(TestEntityType, crudContext) {
      override def findAll(criteria: SQLiteCriteria) = {
        val result = super.findAll(criteria)
        val CursorStream(cursor, _) = result
        cursors += cursor
        result
      }
    }
    val writable = TestEntityType.newWritable
    TestEntityType.copy(unitAsRef, writable)
    val id = persistence.save(None, writable)
    val uri = persistence.toUri(id)
    persistence.find(uri)
    persistence.findAll(new SQLiteCriteria())
    cursors.size must be (2)
    persistence.close()
    for (cursor <- cursors.toList) {
      cursor.isClosed must be (true)
    }
  }

  @Test
  def shouldRefreshCursorWhenDeletingAndSaving() {
    val listView = mock[ListView]
    val activity = new CrudListActivity(TestEntityType, application) {
      private var listAdapter: ListAdapter = _
      override def setListAdapter(adapter: ListAdapter) {
        super.setListAdapter(adapter)
        this.listAdapter = adapter
      }
      override def getListAdapter = listAdapter

      override def getListView = listView
    }
    val observer = mock[DataSetObserver]

    val crudContext = new CrudContext(activity, application)
    TestEntityType.setListAdapterUsingUri(crudContext, activity)
    val listAdapter = activity.getListAdapter
    listAdapter.getCount must be (0)

    val writable = TestEntityType.newWritable
    TestEntityType.copy(unitAsRef, writable)
    val id = TestEntityType.withEntityPersistence(crudContext) { _.save(None, writable) }
    //it must have refreshed the listAdapter
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    TestEntityType.copy(Map("age" -> 50), writable)
    listAdapter.registerDataSetObserver(observer)
    TestEntityType.withEntityPersistence(crudContext) { _.save(Some(id), writable) }
    //it must have refreshed the listAdapter (notified the observer)
    listAdapter.unregisterDataSetObserver(observer)
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    TestEntityType.withEntityPersistence(crudContext) { _.delete(TestEntityType.toUri(id)) }
    //it must have refreshed the listAdapter
    listAdapter.getCount must be (0)
  }

  @Test
  def tableNameMustNotBeReservedWord() {
    tableNameMustNotBeReservedWord("Group")
    tableNameMustNotBeReservedWord("Table")
    tableNameMustNotBeReservedWord("index")
  }

  def tableNameMustNotBeReservedWord(name: String) {
    val persistence = new SQLiteEntityPersistence(new SQLiteCrudType with HiddenEntityType {
      def entityName = name

      def valueFields = List.empty[BaseField]
    }, mock[CrudContext])
    persistence.tableName must be (name + "0")
  }
}

class MyContextWithVars extends Activity with ContextWithVars