package com.github.scrud.android

import _root_.android.provider.BaseColumns
import _root_.android.app.Activity
import _root_.android.database.{Cursor, DataSetObserver}
import _root_.android.widget.ListView
import _root_.android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import action.ContextWithState
import com.github.scrud.state._
import com.github.scrud.persistence._
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.RobolectricTestRunner
import org.scalatest.matchers.MustMatchers
import com.github.triangle._
import com.github.scrud.android.persistence.CursorField._
import persistence.CursorStream
import persistence.SQLiteCriteria
import PortableField._
import scala.collection._
import org.mockito.{Mockito, Matchers}
import Mockito._
import com.github.scrud.util.{ListenerSet, CrudMockitoSugar}
import com.github.scrud._
import com.github.scrud.EntityName

/** A test for [[com.github.scrud.android.SQLitePersistenceFactorySpec]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[RobolectricTestRunner])
class SQLitePersistenceFactorySpec extends MustMatchers with CrudMockitoSugar with Logging {
  protected def logTag = getClass.getSimpleName

  val runningOnRealAndroid: Boolean = try {
    debug("Seeing if running on Real Android...")
    Class.forName("com.xtremelabs.robolectric.RobolectricTestRunner")
    warn("NOT running on Real Android.")
    false
  } catch {
    case _: Throwable => {
      info("Running on Real Android.")
      true
    }
  }

  object TestEntity extends EntityName("Test")

  object TestEntityType extends EntityType(TestEntity) {
    val valueFields = List(persisted[Int]("age") + default(21))
  }

  object TestCrudType extends CrudType(TestEntityType, SQLitePersistenceFactory)

  object TestApplication extends CrudApplication {
    val name = "Test Application"

    def allCrudTypes = List(TestCrudType)

    def dataVersion = 1
  }
  val application = TestApplication
  val listenerSet = mock[ListenerSet[DataListener]]
  when(listenerSet.listeners).thenReturn(Set.empty[DataListener])

  @Test
  def shouldUseCorrectColumnNamesForFindAll() {
    val crudContext = mock[CrudContext]
    stub(crudContext.application).toReturn(application)

    val persistence = new SQLiteEntityPersistence(TestEntityType, crudContext, mock[SQLiteOpenHelper], listenerSet)
    persistence.entityTypePersistedInfo.queryFieldNames must contain(BaseColumns._ID)
    persistence.entityTypePersistedInfo.queryFieldNames must contain("age")
  }

  @Test
  def shouldCloseCursorsWhenClosing() {
    val crudContext = mock[CrudContext]
    stub(crudContext.activityState).toReturn(new State {})
    stub(crudContext.application).toReturn(application)

    val cursors = mutable.Buffer[Cursor]()
    val persistence = new SQLiteEntityPersistence(TestEntityType, crudContext, new GeneratedDatabaseSetup(crudContext), listenerSet) {
      override def findAll(criteria: SQLiteCriteria) = {
        val result = super.findAll(criteria)
        val CursorStream(cursor, _) = result
        cursors += cursor
        result
      }
    }
    val writable = application.newWritable(TestEntityType)
    TestEntityType.copy(PortableField.UseDefaults, writable)
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
    val activity = new CrudListActivity {
      override val applicationState = new State {}
      override def crudApplication = application
      override val getListView: ListView = new ListView(this)
    }
    val observer = mock[DataSetObserver]

    val crudContext = new CrudContext(activity, application)
    activity.setListAdapterUsingUri(crudContext, activity)
    val listAdapter = activity.getListView.getAdapter
    listAdapter.getCount must be (0)

    val writable = application.newWritable(TestEntityType)
    TestEntityType.copy(PortableField.UseDefaults, writable)
    val id = TestCrudType.withEntityPersistence(crudContext) { _.save(None, writable) }
    //it must have refreshed the listAdapter
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    TestEntityType.copy(Map("age" -> 50), writable)
    listAdapter.registerDataSetObserver(observer)
    TestCrudType.withEntityPersistence(crudContext) { _.save(Some(id), writable) }
    //it must have refreshed the listAdapter (notified the observer)
    listAdapter.unregisterDataSetObserver(observer)
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    TestCrudType.withEntityPersistence(crudContext) { _.delete(TestEntityType.toUri(id)) }
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
    SQLitePersistenceFactory.toTableName(EntityName(name)) must be (name + "0")
  }

  @Test
  def onCreateShouldCreateTables() {
    val context = mock[MyContextWithVars]
    val dbSetup = new GeneratedDatabaseSetup(CrudContext(context, application))
    val db = mock[SQLiteDatabase]
    dbSetup.onCreate(db)
    verify(db, times(1)).execSQL(Matchers.contains("CREATE TABLE IF NOT EXISTS"))
  }

  @Test
  def onUpgradeShouldCreateMissingTables() {
    val context = mock[MyContextWithVars]
    val dbSetup = new GeneratedDatabaseSetup(CrudContext(context, application))
    val db = mock[SQLiteDatabase]
    dbSetup.onUpgrade(db, 1, 2)
    verify(db, times(1)).execSQL(Matchers.contains("CREATE TABLE IF NOT EXISTS"))
  }
}

class MyContextWithVars extends Activity with ContextWithState{
  val applicationState = new State {}
}
