package com.github.scrud.android

import _root_.android.app.Activity
import com.github.scrud.android.action.AndroidOperation
import org.junit.Test
import AndroidOperation._
import _root_.android.widget.{BaseAdapter, AdapterView, ListAdapter}
import com.github.scrud._
import org.mockito.Mockito._
import com.github.scrud.persistence._
import com.github.scrud.util.ListenerHolder
import org.mockito.Matchers._
import _root_.android.content.Intent
import _root_.android.view.{LayoutInflater, View, ContextMenu}
import _root_.android.util.SparseArray
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.action.CrudOperation
import com.github.scrud.platform.representation.EditUI
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.android.generate.CrudUIGeneratorForTesting
import org.robolectric.tester.android.view.TestMenu
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import com.github.scrud.android.view.AndroidConversions

/** A test for [[com.github.scrud.android.CrudActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class CrudActivitySpec extends CrudUIGeneratorForTesting with ScrudRobolectricSpec {
  val persistenceFactory = ListBufferPersistenceFactoryForTesting
  val listAdapter = mock[ListAdapter]

  @Test
  def shouldSaveOnBackPressed() {
    val entity = new MapStorage(_entityType.name -> Some("Bob"), _entityType.age -> Some(25))
    val uri = UriPath(_entityType.entityName)
    val intent = new Intent(CreateActionName, AndroidConversions.toUri(uri, classOf[CrudAndroidApplicationForRobolectric]))
    val activityController = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(intent).create().start().visible()
    val activity = activityController.get()
    val commandContext = activity.commandContext
    _entityType.copyAndUpdate(MapStorage, entity, uri, EditUI, activity, commandContext)
    // This should cause it to save and change the currentUriPath to include the id.
    activity.onBackPressed()
    commandContext.waitUntilIdle()

    val results = commandContext.findAll(_entityType.toUri, MapStorage)
    val updatedUri = activity.currentUriPath
    val idOpt = UriPath.findId(updatedUri, _entityType.entityName)
    idOpt must be ('defined)
    results must be (Seq(new MapStorage(_entityType.id -> idOpt, _entityType.name -> Some("Bob"),
      _entityType.age -> Some(25))))
  }

  @Test
  def onPauseShouldNotCreateANewIdEveryTime() {
    val entity = new MapStorage(_entityType.name -> Some("Bob"), _entityType.age -> Some(25))
    val uri = UriPath(_entityType.entityName)
    val activityController = Robolectric.buildActivity(classOf[CrudActivityForRobolectric])
    val activity = activityController.get()
    activityController.withIntent(new Intent(AndroidOperation.CreateActionName, AndroidConversions.toUri(uri, activity))).create()
    //simulate a user entering data
    _entityType.copyAndUpdate(MapStorage, entity, uri, EditUI, activity, activity.commandContext)
    activity.onBackPressed()
    activity.commandContext.waitUntilIdle()

    val uriPathAfterFirstSave = activity.currentUriPath
    //simulate saving again
    activity.onBackPressed()
    activity.commandContext.waitUntilIdle()

    activity.currentUriPath must be (uriPathAfterFirstSave)
    val results = activity.commandContext.findAll(_entityType.toUri, MapStorage)
    results.size must be (1)
  }

  @Test
  def mustBeConstructibleWithoutAnApplicationYet() {
    new CrudActivity
  }

  @Test
  def mustNotCopyFromUpstreamEntityIfUriPathIsInsufficient() {
    val persistenceForParent = mock[ThinPersistence]
    val parentEntityName = EntityName("Parent")
    val entityType1 = new EntityTypeForTesting {
      override lazy val referencedEntityNames: Seq[EntityName] = Seq(parentEntityName)
    }
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val entityTypeMap = new EntityTypeMapForTesting(entityType1 -> entityType1.platformDriver.localDatabasePersistenceFactory,
      parentEntityType -> new PersistenceFactoryForTesting(persistenceForParent))

    val application = new CrudAndroidApplication(entityTypeMap)
    val activity = new CrudActivityForTesting(application) {
      override def currentUriPath = entityType1.toUri
    }
    activity.populateFromReferencedEntities()
    verify(persistenceForParent, never()).findAll(any())
  }

  @Test
  def shouldHaveCorrectOptionsMenuInUpdate() {
    val data = new MapStorage(_entityType.name -> Some("Bob"), _entityType.age -> Some(25))
    val activityController = Robolectric.buildActivity(classOf[CrudActivityForRobolectric])
    val activity = activityController.get()
    val commandContext = activity.commandContext

    val id = commandContext.save(EntityTypeForTesting.entityName, None, MapStorage, data)

    activityController.withIntent(new Intent(UpdateActionName, AndroidConversions.toUri(_entityType.toUri(id), activity)))
    activityController.create().start()
    commandContext.waitUntilIdle()

    val menu = new TestMenu(activity)
    activity.onCreateOptionsMenu(menu)
    val items = 0.to(menu.size() - 1).map(menu.getItem(_))
    items.map(_.getTitle.toString) must be (Seq("Add My Map", "My Map List", "Delete"))
    activity.onOptionsItemSelected(items.last) must be (true)
  }

  @Test
  def shouldHaveCorrectContextMenu() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(ListActionName)).create().get()
    activity.onCreateContextMenu(contextMenu, ignoredView, ignoredMenuInfo)
    verify(contextMenu).add(0, R.string.my_map_list, 0, R.string.my_map_list)
    verify(contextMenu).add(0, R.string.edit_my_map, 1, R.string.edit_my_map)
    verify(contextMenu).add(0, R.string.delete_item, 2, R.string.delete_item)
  }

  @Test
  def shouldHandleNoEntityOptions() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null

    val _entityType = new EntityTypeForTesting
    val entityTypeMap = new EntityTypeMapForTesting(_entityType)
    val entityNavigation = new EntityNavigationForTesting(entityTypeMap) {
      override def actionsFromCrudOperation(crudOperation: CrudOperation) = Nil
    }
    val application = new CrudAndroidApplication(entityNavigation)
    val activity = new CrudActivityForTesting(application)
    //shouldn't do anything
    activity.onCreateContextMenu(contextMenu, ignoredView, ignoredMenuInfo)
  }

  lazy val sparseArrayWorking: Boolean = {
    val array = new SparseArray[String]()
    array.put(0, "hello")
    val working = array.get(0) == "hello"
    if (working) sys.error("SparseArray is now working!  You must have upgraded to a robolectric version that supports it.  Delete this code.")
    working
  }

  @Test
  def shouldRefreshOnRestart() {
    val data = new MapStorage(_entityType.name -> Some("Bob"), _entityType.age -> Some(25))
    val activityController = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(ListActionName)).create().start()
    val activity = activityController.get()

    activity.commandContext.save(EntityTypeForTesting.entityName, None, MapStorage, data)
    activity.commandContext.waitUntilIdle()
    activity.listAdapter.getCount must be (1)

    activityController.stop()  // this should prevent a database read.
    val data2 = new MapStorage(_entityType.name -> Some("Will"), _entityType.age -> Some(31))
    activity.commandContext.save(EntityTypeForTesting.entityName, None, MapStorage, data2)
    activity.commandContext.waitUntilIdle()
    activity.listAdapter.getCount must be (1) // Should not have refreshed yet since paused.

    activityController.restart() // this should cause a database read.
    activity.commandContext.waitUntilIdle()
    activity.listAdapter.getCount must be (2)
  }

  @Test
  def shouldIgnoreClicksOnHeader() {
    val _entityType = EntityTypeForTesting
    val application = new CrudAndroidApplication(new EntityTypeMapForTesting(_entityType))
    val activity = new CrudActivityForTesting(application)
    // should do nothing
    activity.onListItemClick(null, null, -1, -1)
  }

  val seqPersistence = mock[TypedCrudPersistence[Map[String,Any]]]
  val adapterView = mock[AdapterView[BaseAdapter]]
  val activity = mock[Activity]
  val listAdapterCapture = capturingAnswer[Unit] { Unit }
  val generatedEntityName = EntityName("Generated")
  val layoutInflater = mock[LayoutInflater]
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

}