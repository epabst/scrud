package com.github.scrud.android

import _root_.android.app.Activity
import com.github.scrud.android.action.AndroidOperation
import org.junit.Test
import org.junit.runner.RunWith
import com.github.scrud.android.persistence.CursorField
import scala.collection.mutable
import org.scalatest.matchers.MustMatchers
import AndroidOperation._
import _root_.android.widget.{BaseAdapter, AdapterView, ListAdapter}
import com.github.scrud._
import org.mockito.Mockito._
import com.github.scrud.persistence._
import com.github.scrud.util.{ListenerHolder, CrudMockitoSugar, ReadyFuture}
import org.mockito.Matchers._
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.state.State
import _root_.android.content.Intent
import com.xtremelabs.robolectric.tester.android.view.TestMenu
import _root_.android.view.{LayoutInflater, View, ContextMenu}
import _root_.android.util.SparseArray
import com.github.triangle.PortableField._
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.action.CrudOperation

/** A test for [[com.github.scrud.android.CrudActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudActivitySpec extends CrudMockitoSugar with MustMatchers {
  val persistenceFactory = mock[PersistenceFactory]
  val persistence = mock[CrudPersistence]
  val listAdapter = mock[ListAdapter]

  @Test
  def shouldSupportAddingWithoutEverFinding() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
      override lazy val currentUriPath = uri
      override lazy val crudContext = new AndroidCrudContext(this, crudApplication) {
        override def future[T](body: => T) = new ReadyFuture[T](body)
      }
    }
    activity.onCreate(null)
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    verify(persistence).save(None, Map[String,Option[Any]](CursorField.idFieldName -> None, "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString)))
    verify(persistence, never()).find(uri)
  }

  @Test
  def shouldAddIfIdNotFound() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
      override lazy val currentUriPath = uri
      override lazy val crudContext = new AndroidCrudContext(this, crudApplication) {
        override def future[T](body: => T) = new ReadyFuture[T](body)
      }
    }
    when(persistence.find(uri)).thenReturn(None)
    activity.onCreate(null)
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    verify(persistence).save(None, mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString), CursorField.idFieldName -> None))
  }

  @Test
  def shouldAllowUpdating() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val entityType = _crudType.entityType
    val uri = UriPath(entityType.entityName) / 101
    stub(persistence.find(uri)).toReturn(Some(entity))
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
      override lazy val currentUriPath = uri
      override lazy val crudContext = new AndroidCrudContext(this, crudApplication) {
        override def future[T](body: => T) = new ReadyFuture[T](body)
      }
    }
    activity.onCreate(null)
    val viewData = entityType.copyAndUpdate(activity, mutable.Map[String,Option[Any]]())
    viewData.apply("name") must be (Some("Bob"))
    viewData.apply("age") must be (Some(25))

    activity.onBackPressed()
    verify(persistence).save(Some(101),
      mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString), CursorField.idFieldName -> Some(101)))
  }

  @Test
  def withPersistenceShouldClosePersistence() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
    }
    activity.crudContext.withEntityPersistence(_crudType.entityType) { p => p.findAll(UriPath.EMPTY) }
    verify(persistence).close()
  }

  @Test
  def withPersistenceShouldClosePersistenceWithFailure() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
    }
    try {
      activity.crudContext.withEntityPersistence(_crudType.entityType) { p => throw new IllegalArgumentException("intentional") }
      fail("should have propogated exception")
    } catch {
      case e: IllegalArgumentException => "expected"
    }
    verify(persistence).close()
  }

  @Test
  def onPauseShouldNotCreateANewIdEveryTime() {
    val _crudType = new CrudTypeForTesting(persistence)
    val application = new CrudApplicationForTesting(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    when(persistence.find(uri)).thenReturn(None)
    stub(persistence.save(None, mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString), CursorField.idFieldName -> None))).toReturn(101)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
      override lazy val crudContext = new AndroidCrudContext(this, crudApplication) {
        override def future[T](body: => T) = new ReadyFuture[T](body)
      }
    }
    activity.setIntent(constructIntent(AndroidOperation.CreateActionName, uri, activity, null))
    activity.onCreate(null)
    //simulate a user entering data
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    activity.onBackPressed()
    verify(persistence, times(1)).save(None, mutable.Map[String,Option[Any]](CursorField.idFieldName -> None, "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString)))
    //all but the first time should provide an id
    verify(persistence).save(Some(101), mutable.Map[String,Option[Any]](CursorField.idFieldName -> Some(101), "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some((uri / 101).toString),
      CursorField.idFieldName -> Some(101)))
  }

  @Test
  def mustBeConstructibleWithoutAnApplicationYet() {
    new CrudActivity
  }

  @Test
  def mustNotCopyFromParentEntityIfUriPathIsInsufficient() {
    val persistenceFactory = mock[PersistenceFactory]
    val parentEntityName = EntityName("Parent")
    val entityType = new EntityTypeForTesting {
      override lazy val parentEntityNames = Seq(parentEntityName)
    }
    val crudType = new CrudType(entityType, persistenceFactory)
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val parentCrudType = new CrudType(parentEntityType, persistenceFactory)
    val application = new CrudApplicationForTesting(crudType, parentCrudType)
    stub(persistenceFactory.maySpecifyEntityInstance(eql(entityType.entityName), any())).toReturn(false)

    val activity = new CrudActivityForTesting(application)
    activity.populateFromParentEntities()
    verify(persistenceFactory, never()).createEntityPersistence(any(), any())
  }

  @Test
  def shouldAllowAdding() {
    val persistence = mock[CrudPersistence]
    val entityType = new EntityTypeForTesting
    val crudType = new CrudTypeForTesting(entityType, persistence)
    val application = new CrudApplicationForTesting(crudType)
    when(persistence.entityType).thenReturn(entityType)
    when(persistence.findAll(any())).thenReturn(Seq(Map[String,Any]("name" -> "Bob", "age" -> 25)))
    val activity = new CrudActivityForTesting(application) {
      override lazy val crudContext = new AndroidCrudContext(this, application) {
        /**
         * Handle the exception by communicating it to the user and developers.
         */
        override def reportError(throwable: Throwable) {
          throw throwable
        }
      }
      override lazy val applicationState = new State
    }
    activity.setIntent(new Intent(Intent.ACTION_MAIN))
    activity.onCreate(null)
    val menu = new TestMenu(activity)
    activity.onCreateOptionsMenu(menu)
    val item0 = menu.getItem(0)
    item0.getTitle.toString must be ("Add")
    menu.size must be (1)

    activity.onOptionsItemSelected(item0) must be (true)
  }

  @Test
  def shouldHaveCorrectContextMenu() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null
    val _crudType = CrudTypeForTesting
    val application = new CrudApplicationForTesting(_crudType)
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
    }
    activity.onCreateContextMenu(contextMenu, ignoredView, ignoredMenuInfo)
    verify(contextMenu).add(0, res.R.string.delete_item, 0, res.R.string.delete_item)
  }

  @Test
  def shouldHandleNoEntityOptions() {
    val contextMenu = mock[ContextMenu]
    val ignoredView: View = null
    val ignoredMenuInfo: ContextMenu.ContextMenuInfo = null

    val _crudType = new CrudTypeForTesting(new EntityTypeForTesting)
    val application = new CrudApplicationForTesting(_crudType) {
      override def actionsFromCrudOperation(crudOperation: CrudOperation) = Nil
    }
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
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
    val persistenceFactory = mock[PersistenceFactory]
    val persistence = mock[CrudPersistence]
    stub(persistenceFactory.createEntityPersistence(anyObject(), anyObject())).toReturn(persistence)
    when(persistence.findAll(any())).thenReturn(Seq(Map[String,Any]("name" -> "Bob", "age" -> 25)))
    val entityType = new EntityTypeForTesting
    val _crudType = new CrudTypeForTesting(entityType, persistenceFactory)
    val application = new CrudApplicationForTesting(_crudType)
    class SomeCrudListActivity extends CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType

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
    val _crudType = CrudTypeForTesting
    val activity = new CrudActivityForTesting(application) {
      override lazy val entityType = _crudType.entityType
    }
    // should do nothing
    activity.onListItemClick(null, null, -1, -1)
  }

  val seqPersistence = mock[SeqCrudPersistence[Map[String,Any]]]
  val adapterView = mock[AdapterView[BaseAdapter]]
  val activity = mock[Activity]
  val listAdapterCapture = capturingAnswer[Unit] { Unit }
  val generatedEntityName = EntityName("Generated")
  val crudContext = mock[AndroidCrudContext]
  val layoutInflater = mock[LayoutInflater]
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

  @Test
  def itsListAdapterMustGetTheItemIdUsingTheIdField() {
    val factory = new GeneratedPersistenceFactory[Map[String,Any]] {
      def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = seqPersistence
    }
    val entityType = new EntityType(generatedEntityName, TestingPlatformDriver) {
      override protected val idField = mapField[ID]("longId") + super.idField
      def valueFields = Nil
    }
    val _crudApplication = new CrudApplicationForTesting(CrudType(entityType, factory))
    stub(crudContext.activityState).toReturn(new State)
    stub(crudContext.applicationState).toReturn(new State)
    stub(crudContext.dataListenerHolder(entityType)).toReturn(dataListenerHolder)
    when(adapterView.setAdapter(anyObject())).thenAnswer(listAdapterCapture)
    val persistence = mock[CrudPersistence]
    when(crudContext.openEntityPersistence(entityType)).thenReturn(persistence)
    val uri = UriPath.EMPTY
    when(persistence.entityType).thenReturn(entityType)
    when(persistence.findAll(uri)).thenReturn(List(Map("longId" -> 456L)))
    val activity = new CrudActivityForTesting(_crudApplication)
    activity.setListAdapter(adapterView, entityType, crudContext, new CrudContextItems(uri, crudContext), activity, 123)
    verify(adapterView).setAdapter(anyObject())
    val listAdapter = listAdapterCapture.params(0).asInstanceOf[ListAdapter]
    listAdapter.getItemId(0) must be (456L)
  }
}