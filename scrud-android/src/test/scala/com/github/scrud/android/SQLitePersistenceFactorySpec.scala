package com.github.scrud.android

import _root_.android.content.Intent
import _root_.android.provider.BaseColumns
import _root_.android.database.{Cursor, DataSetObserver}
import com.github.scrud.persistence._
import org.junit.Test
import org.junit.runner.RunWith
import persistence.CursorStream
import scala.collection._
import com.github.scrud._
import com.github.scrud.types.TitleQT
import com.github.scrud.copy.types.{MapStorage, Default}
import com.github.scrud.platform.representation.{EditUI, DetailUI}
import com.github.scrud.copy.SourceType
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import com.github.scrud.android.action.AndroidOperation._
import org.mockito.Mockito._
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.android.persistence.EntityTypePersistedInfo

/** A test for [[com.github.scrud.android.SQLitePersistenceFactorySpec]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class SQLitePersistenceFactorySpec extends ScrudRobolectricSpec {
  @Test
  def shouldUseCorrectColumnNamesForFindAll() {
    val entityTypePersistedInfo = EntityTypePersistedInfo(new EntityTypeForTesting {
      field("unpersisted", TitleQT, Seq(EditUI, DetailUI, Default("hello")))
    })
    entityTypePersistedInfo.queryFieldNames must contain(BaseColumns._ID)
    entityTypePersistedInfo.queryFieldNames must contain("age")
    entityTypePersistedInfo.queryFieldNames must not(contain("unpersisted"))
  }

  @Test
  def shouldCloseCursorsWhenClosing() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistenceFactory = SQLitePersistenceFactory
    val cursors = mutable.Buffer[Cursor]()
    val database = new GeneratedDatabaseSetup(commandContext, persistenceFactory).getWritableDatabase
    val listenerSet = persistenceFactory.listenerSet(EntityTypeForTesting, commandContext.sharedContext)
    val thinPersistence = new SQLiteCrudPersistence(EntityTypeForTesting, database, commandContext,
      listenerSet)
    val sharedContext = commandContext.sharedContext
    val persistence = new CrudPersistenceUsingThin(EntityTypeForTesting, thinPersistence, sharedContext, listenerSet) {
      override def findAll(uri: UriPath) = {
        val result = super.findAll(uri)
        val CursorStream(cursor, _, _) = result
        cursors += cursor
        result
      }
    }
    //UseDefaults is provided here in the item list for the sake of PortableField.adjustment[SQLiteCriteria] fields
    val id = commandContext.save(EntityForTesting, None, SourceType.none, SourceType.none)
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
  def shouldRefreshCursorWhenSavingAndDeleting() {
    val activityController = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(ListActionName))
    val activity = activityController.create().get()
    val observer = mock[DataSetObserver]
    val commandContext = activity.commandContext

    activityController.start()
    activity.listAdapter.getCount must be (0)
    activity.listAdapter.registerDataSetObserver(observer)
    verify(observer, never()).onChanged()

    val id = commandContext.save(EntityForTesting, None, MapStorage, new MapStorage(EntityTypeForTesting.name -> Some("Gilbert")))
    commandContext.waitUntilIdle()
    //it must have refreshed the listAdapter (notified the observer)
    activity.listAdapter.getCount must be (1)
    verify(observer, times(1)).onChanged()

    commandContext.save(EntityForTesting, Some(id), MapStorage, new MapStorage(EntityTypeForTesting.name -> Some("Patricia")))
    commandContext.waitUntilIdle()
    //it must have refreshed the listAdapter (notified the observer)
    verify(observer, times(2)).onChanged()
    activity.listAdapter.getCount must be (1)

    commandContext.delete(EntityForTesting.toUri(id)) must be (1)
    commandContext.waitUntilIdle()
    verify(observer, times(3)).onChanged()
    //it must have refreshed the listAdapter
    activity.listAdapter.getCount must be (0)
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
