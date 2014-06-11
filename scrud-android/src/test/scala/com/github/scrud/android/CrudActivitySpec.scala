package com.github.scrud.android

import _root_.android.app.Activity
import com.github.scrud.android.action.AndroidOperation
import org.junit.Test
import org.junit.runner.RunWith
import com.github.scrud.android.persistence.CursorField
import org.scalatest.matchers.MustMatchers
import AndroidOperation._
import _root_.android.widget.{BaseAdapter, AdapterView, ListAdapter}
import com.github.scrud._
import org.mockito.Mockito._
import com.github.scrud.persistence._
import com.github.scrud.util.{ListenerHolder, CrudMockitoSugar}
import org.mockito.Matchers._
import com.github.scrud.state.State
import _root_.android.content.Intent
import com.xtremelabs.robolectric.tester.android.view.TestMenu
import _root_.android.view.{LayoutInflater, View, ContextMenu}
import _root_.android.util.SparseArray
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.action.CrudOperation
import com.github.scrud.platform.representation.{EditUI, Persistence}
import com.github.scrud.copy.types.MapStorage

/** A test for [[com.github.scrud.android.CrudActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudActivitySpec extends CrudMockitoSugar with MustMatchers {
  val persistenceFactory = ListBufferPersistenceFactoryForTesting
  val listAdapter = mock[ListAdapter]

  @Test
  def shouldSaveOnBackPressed() {
    val _entityType = EntityTypeForTesting
    val application = new CrudApplicationForTesting(_entityType -> persistenceFactory)
    val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_entityType.entityName)
    val activity = new CrudActivityForTesting {
      override lazy val entityType = _entityType
      override protected lazy val initialUriPath = uri
      override lazy val commandContext = new AndroidCommandContextForTesting(crudApplication, this)
    }
    activity.onCreate(null)
    _entityType.copyAndUpdate(Persistence.Latest, entity, uri, EditUI, activity, activity.commandContext)
    activity.onBackPressed()
    val results = activity.commandContext.persistenceConnection.findAll(_entityType.toUri, MapStorage, activity.commandContext)
    val idOpt = activity.currentUriPath.findId(_entityType.entityName)
    idOpt must be ('defined)
    results must be (Seq(Map[String,Option[Any]](CursorField.idFieldName -> idOpt, "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString))))
  }

  @Test
  def onPauseShouldNotCreateANewIdEveryTime() {
    val _entityType = EntityTypeForTesting
    val application = new CrudApplicationForTesting(_entityType -> persistenceFactory)
    val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_entityType.entityName)
    val activity = new CrudActivityForTesting {
      override lazy val entityType = _entityType
      override protected lazy val initialUriPath = uri
      override lazy val commandContext = new AndroidCommandContextForTesting(crudApplication, this)
    }
    activity.setIntent(constructIntent(AndroidOperation.CreateActionName, uri, activity, null))
    activity.onCreate(null)
    //simulate a user entering data
    _entityType.copyAndUpdate(Persistence.Latest, entity, uri, EditUI, activity, activity.commandContext)
    activity.onBackPressed()
    val uriPathAfterFirstSave = activity.currentUriPath
    //simulate saving again
    activity.onBackPressed()
    activity.currentUriPath must be (uriPathAfterFirstSave)
    val results = activity.commandContext.persistenceConnection.findAll(_entityType.toUri, MapStorage, activity.commandContext)
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
    val application = new CrudApplicationForTesting(entityType1 -> new PersistenceFactoryForTesting(entityType1),
      parentEntityType -> new PersistenceFactoryForTesting(parentEntityType, persistenceForParent))

    val activity = new CrudActivityForTesting {
      override def currentUriPath = UriPath(entityType1.entityName)
    }
    activity.populateFromReferencedEntities()
    verify(persistenceForParent, never()).findAll(any())
  }

  @Test
  def shouldHaveCorrectOptionsMenu() {
    val persistence = mock[ThinPersistence]
    val _entityType = EntityTypeForTesting
    val application = new CrudApplicationForTesting(_entityType -> new PersistenceFactoryForTesting(_entityType, persistence))
    when(persistence.findAll(any())).thenReturn(Seq(Map[String,Any](CursorField.idFieldName -> Some(400L), "name" -> Some("Bob"), "age" -> Some(25), "uri" -> None)))
    val activity = new CrudActivityForTesting {
      override lazy val currentAction = UpdateActionName
      override lazy val commandContext = new AndroidCommandContextForTesting(application, this)
      override lazy val applicationState = new State
    }
    activity.setIntent(new Intent(Intent.ACTION_MAIN))
    activity.onCreate(null)
    val menu = new TestMenu(activity)
    activity.onCreateOptionsMenu(menu)
    val item0 = menu.getItem(0)
    item0.getTitle.toString must be ("Delete")
    menu.size must be (1)

    activity.onOptionsItemSelected(item0) must be (true)
  }

  @Test
  def shouldHaveCorrectContextMenu() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null
    val _entityType = EntityTypeForTesting
    val application = new CrudApplicationForTesting(_entityType)
    val activity = new CrudActivityForTesting {
      override lazy val entityType = _entityType
      override lazy val currentAction = ListActionName
    }
    activity.onCreateContextMenu(contextMenu, ignoredView, ignoredMenuInfo)
    verify(contextMenu).add(0, res.R.string.edit_test, 0, res.R.string.edit_test)
    verify(contextMenu).add(0, res.R.string.delete_item, 1, res.R.string.delete_item)
  }

  @Test
  def shouldHandleNoEntityOptions() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null

    val _entityType = new EntityTypeForTesting
    val entityTypeMap = new EntityTypeMapForTesting(_entityType)
    val application = new EntityNavigationForTesting(entityTypeMap) {
      override def actionsFromCrudOperation(crudOperation: CrudOperation) = Nil
    }
    val activity = new CrudActivityForTesting {
      override lazy val entityType = _entityType
    }
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
  def shouldRefreshOnResume() {
    val persistence = mock[ThinPersistence]
    when(persistence.findAll(any())).thenReturn(Seq(Map[String,Any]("name" -> "Bob", "age" -> 25)))
    val _entityType = new EntityTypeForTesting
    val application = new CrudApplicationForTesting(_entityType -> new PersistenceFactoryForTesting(_entityType, persistence))
    class SomeCrudListActivity extends CrudActivityForTesting {
      override lazy val entityType = _entityType

      //make it public for testing
      override def onPause() {
        super.onPause()
      }

      //make it public for testing
      override def onResume() {
        super.onResume()
      }
    }
    val activity = new SomeCrudListActivity
    activity.setIntent(new Intent(Intent.ACTION_MAIN))
    activity.onCreate(null)
    activity.onPause()
    //verify(persistenceFactory, never()).refreshAfterDataChanged(anyObject())

    activity.onResume()
    //verify(persistenceFactory, times(1)).refreshAfterDataChanged(anyObject())
  }

  @Test
  def shouldIgnoreClicksOnHeader() {
    val application = mock[CrudApplication]
    val _entityType = EntityTypeForTesting
    val activity = new CrudActivityForTesting {
      override lazy val entityType = _entityType
    }
    // should do nothing
    activity.onListItemClick(null, null, -1, -1)
  }

  val seqPersistence = mock[SeqCrudPersistence[Map[String,Any]]]
  val adapterView = mock[AdapterView[BaseAdapter]]
  val activity = mock[Activity]
  val listAdapterCapture = capturingAnswer[Unit] { Unit }
  val generatedEntityName = EntityName("Generated")
  val layoutInflater = mock[LayoutInflater]
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

}