package com.github.scrud.android

import _root_.android.provider.BaseColumns
import _root_.android.app.Activity
import _root_.android.database.{Cursor, DataSetObserver}
import _root_.android.widget.ListView
import com.github.scrud.state._
import com.github.scrud.persistence._
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import persistence.{EntityTypePersistedInfo, CursorStream}
import scala.collection._
import org.mockito.Mockito
import Mockito._
import com.github.scrud.util.{Logging, CrudMockitoSugar}
import com.github.scrud._
import com.github.scrud.EntityName
import state.ActivityStateHolder
import com.github.scrud.android.testres.R
import com.github.scrud.types.NaturalIntQT
import com.github.scrud.copy.types.{MapStorage, Default}
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.context.SharedContextForTesting
import com.github.scrud.copy.SourceType

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
    val age = field("age", NaturalIntQT, Seq(Persistence(1), Default(21)))
  }

  val entityTypeMap = EntityTypeMapForTesting(Seq(TestEntityType -> SQLitePersistenceFactory))

  object TestApplication extends CrudApplication(androidPlatformDriver, entityTypeMap) {
    val name = "Test Application"
  }
  val application = TestApplication

  @Test
  def shouldUseCorrectColumnNamesForFindAll() {
    val commandContext = mock[AndroidCommandContext]
    stub(commandContext.application).toReturn(application)
    stub(commandContext.entityTypeMap).toReturn(entityTypeMap)

    val entityTypePersistedInfo = EntityTypePersistedInfo(TestEntityType)
    entityTypePersistedInfo.queryFieldNames must contain(BaseColumns._ID)
    entityTypePersistedInfo.queryFieldNames must contain("age")
  }

  @Test
  def shouldCloseCursorsWhenClosing() {
    val persistenceFactory = SQLitePersistenceFactory
    val entityTypeMap = EntityTypeMapForTesting(Seq(TestEntityType -> persistenceFactory))
    val commandContext = mock[AndroidCommandContext]
    stub(commandContext.stateHolder).toReturn(new ActivityStateHolderForTesting)
    stub(commandContext.application).toReturn(application)
    stub(commandContext.entityTypeMap).toReturn(entityTypeMap)

    val cursors = mutable.Buffer[Cursor]()
    val database = new GeneratedDatabaseSetup(commandContext, persistenceFactory).getWritableDatabase
    val thinPersistence = new SQLiteThinEntityPersistence(TestEntityType, database, commandContext)
    val sharedContext = new SharedContextForTesting(entityTypeMap)
    val persistence = new CrudPersistenceUsingThin(TestEntityType, thinPersistence, sharedContext) {
      override def findAll(uri: UriPath) = {
        val result = super.findAll(uri)
        val CursorStream(cursor, _, _) = result
        cursors += cursor
        result
      }
    }
    //UseDefaults is provided here in the item list for the sake of PortableField.adjustment[SQLiteCriteria] fields
    val id = commandContext.save(TestEntity, SourceType.none, None, SourceType.none)
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

    val commandContext = new AndroidCommandContextForTesting(application, activity)
    activity.setListAdapterUsingUri(commandContext, activity)
    val listAdapter = activity.getAdapterView.getAdapter
    listAdapter.getCount must be (0)

    val id = commandContext.save(TestEntity, SourceType.none, None, SourceType.none)
    //it must have refreshed the listAdapter
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    listAdapter.registerDataSetObserver(observer)
    commandContext.save(TestEntity, MapStorage, Some(id), new MapStorage(TestEntityType.age -> Some(50)))
    //it must have refreshed the listAdapter (notified the observer)
    listAdapter.unregisterDataSetObserver(observer)
    listAdapter.getCount must be (if (runningOnRealAndroid) 1 else 0)

    commandContext.delete(TestEntity.toUri(id)) must be (1)
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
