package com.github.scrud.android

import action.AndroidOperation
import org.junit.Test
import org.junit.runner.RunWith
import persistence.CursorField
import scala.collection.mutable
import org.scalatest.matchers.MustMatchers
import AndroidOperation._
import android.widget.ListAdapter
import com.github.scrud.{CrudApplication, EntityName, UriPath}
import org.mockito.Mockito._
import com.github.scrud.persistence._
import com.github.scrud.util.{CrudMockitoSugar, ReadyFuture}
import org.mockito.Matchers._
import scala.Some
import com.github.scrud.state.State
import android.content.Intent
import com.xtremelabs.robolectric.tester.android.view.TestMenu
import android.view.{View, ContextMenu}
import com.github.scrud.action.CrudOperation
import android.util.SparseArray

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
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new MyCrudActivity(application) {
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
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new MyCrudActivity(application) {
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
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val entityType = _crudType.entityType
    val uri = UriPath(entityType.entityName) / 101
    stub(persistence.find(uri)).toReturn(Some(entity))
    val activity = new MyCrudActivity(application) {
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
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val activity = new MyCrudActivity(application) {
      override lazy val entityType = _crudType.entityType
    }
    activity.crudContext.withEntityPersistence(_crudType.entityType) { p => p.findAll(UriPath.EMPTY) }
    verify(persistence).close()
  }

  @Test
  def withPersistenceShouldClosePersistenceWithFailure() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val activity = new MyCrudActivity(application) {
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
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
    val uri = UriPath(_crudType.entityType.entityName)
    when(persistence.find(uri)).thenReturn(None)
    stub(persistence.save(None, mutable.Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString), CursorField.idFieldName -> None))).toReturn(101)
    val activity = new MyCrudActivity(application) {
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
    val entityType = new MyEntityType {
      override lazy val parentEntityNames = Seq(parentEntityName)
    }
    val crudType = new CrudType(entityType, persistenceFactory)
    val parentEntityType = new MyEntityType(parentEntityName)
    val parentCrudType = new CrudType(parentEntityType, persistenceFactory)
    val application = MyCrudApplication(crudType, parentCrudType)
    stub(persistenceFactory.maySpecifyEntityInstance(eql(entityType.entityName), any())).toReturn(false)

    val activity = new MyCrudActivity(application)
    activity.populateFromParentEntities()
    verify(persistenceFactory, never()).createEntityPersistence(any(), any())
  }

  @Test
  def shouldAllowAdding() {
    val persistence = mock[CrudPersistence]
    val entityType = new MyEntityType
    val crudType = new MyCrudType(entityType, persistence)
    val application = MyCrudApplication(crudType)
    when(persistence.entityType).thenReturn(entityType)
    when(persistence.findAll(any())).thenReturn(Seq(Map[String,Any]("name" -> "Bob", "age" -> 25)))
    val activity = new MyCrudActivity(application) {
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
    val _crudType = MyCrudType
    val application = MyCrudApplication(_crudType)
    val activity = new MyCrudActivity(application) {
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

    val _crudType = new MyCrudType(new MyEntityType)
    val application = new MyCrudApplication(_crudType) {
      override def actionsFromCrudOperation(crudOperation: CrudOperation) = Nil
    }
    val activity = new MyCrudActivity(application) {
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
    val entityType = new MyEntityType
    val _crudType = new MyCrudType(entityType, persistenceFactory)
    val application = MyCrudApplication(_crudType)
    class SomeCrudListActivity extends MyCrudActivity(application) {
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
    val _crudType = MyCrudType
    val activity = new MyCrudActivity(application) {
      override lazy val entityType = _crudType.entityType
    }
    // should do nothing
    activity.onListItemClick(null, null, -1, -1)
  }
}