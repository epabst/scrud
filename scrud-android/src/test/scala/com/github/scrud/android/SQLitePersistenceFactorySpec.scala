package com.github.scrud.android

import _root_.android.provider.BaseColumns
import _root_.android.app.Activity
import _root_.android.database.{Cursor, DataSetObserver}
import _root_.android.widget.ListView
import com.github.scrud
import com.github.scrud.state._
import com.github.scrud.persistence._
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.triangle._
import com.github.scrud.android.persistence.CursorField._
import persistence.{EntityTypePersistedInfo, CursorStream}
import PortableField._
import res.R
import scala.collection._
import org.mockito.Mockito
import Mockito._
import scrud.util.{MutableListenerSet, CrudMockitoSugar}
import com.github.scrud._
import com.github.scrud.EntityName
import state.ActivityStateHolder

/** A test for [[com.github.scrud.android.SQLitePersistenceFactorySpec]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class SQLitePersistenceFactorySpec extends MustMatchers with CrudMockitoSugar with Logging {
  protected val logTag = getClass.getSimpleName

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

  val androidPlatformDriver = new AndroidPlatformDriver(classOf[R])

  object TestEntity extends EntityName("Test")

  object TestEntityType extends EntityType(TestEntity, androidPlatformDriver) {
    val valueFields = List(persisted[Int]("age") + default(21))
  }

  object TestCrudType extends CrudType(TestEntityType, SQLitePersistenceFactory)

  object TestApplication extends CrudApplication(androidPlatformDriver) {
    val name = "Test Application"

    val allCrudTypes = List(TestCrudType)
  }
  val application = TestApplication
  val listenerSet = mock[MutableListenerSet[DataListener]]
  when(listenerSet.listeners).thenReturn(Set.empty[DataListener])

  @Test
  def shouldUseCorrectColumnNamesForFindAll() {
    val crudContext = mock[AndroidCrudContext]
    stub(crudContext.application).toReturn(application)
    stub(crudContext.persistenceFactoryMapping).toReturn(application)

    val entityTypePersistedInfo = EntityTypePersistedInfo(TestEntityType)
    entityTypePersistedInfo.queryFieldNames must contain(BaseColumns._ID)
    entityTypePersistedInfo.queryFieldNames must contain("age")
  }

  @Test
  def shouldCloseCursorsWhenClosing() {
    val crudContext = mock[AndroidCrudContext]
    stub(crudContext.stateHolder).toReturn(new ActivityStateHolderForTesting)
    stub(crudContext.application).toReturn(application)
    stub(crudContext.persistenceFactoryMapping).toReturn(application)
    val persistenceFactory = SQLitePersistenceFactory

    val cursors = mutable.Buffer[Cursor]()
    val database = new GeneratedDatabaseSetup(crudContext, persistenceFactory).getWritableDatabase
    val thinPersistence = new SQLiteThinEntityPersistence(TestEntityType, database, crudContext)
    val persistence = new CrudPersistenceUsingThin(TestEntityType, thinPersistence, crudContext, listenerSet) {
      override def findAll(uri: UriPath) = {
        val result = super.findAll(uri)
        val CursorStream(cursor, _) = result
        cursors += cursor
        result
      }
    }
    //UseDefaults is provided here in the item list for the sake of PortableField.adjustment[SQLiteCriteria] fields
    val id = persistence.saveCopy(None, PortableField.UseDefaults)
    val uri = persistence.toUri(id)
    persistence.find(uri)
    persistence.findAll(UriPath.EMPTY)
    cursors.size must be (2)
    persistence.close()
    for (cursor <- cursors.toList) {
      cursor.isClosed must be (true)
    }
  }

  @Test
  def shouldRefreshCursorWhenDeletingAndSaving() {
    val activity = new CrudActivityForTesting(application) {
      override val getAdapterView: ListView = new ListView(this)
    }
    val observer = mock[DataSetObserver]

    val crudContext = new AndroidCrudContextForTesting(application, activity)
    activity.setListAdapterUsingUri(crudContext, activity)
    val listAdapter = activity.getAdapterView.getAdapter
    listAdapter.getCount must be (0)

    val id = crudContext.withEntityPersistence(TestEntityType) { _.saveCopy(None, PortableField.UseDefaults) }
    //it must have refreshed the listAdapter
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    listAdapter.registerDataSetObserver(observer)
    crudContext.withEntityPersistence(TestEntityType) { _.saveCopy(Some(id), Map("age" -> Some(50))) }
    //it must have refreshed the listAdapter (notified the observer)
    listAdapter.unregisterDataSetObserver(observer)
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    crudContext.withEntityPersistence(TestEntityType) { _.delete(TestEntityType.toUri(id)) }
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
}

class ActivityStateHolderForTesting extends Activity with ActivityStateHolder {
  val activityState = new State
  val applicationState = new State
}
