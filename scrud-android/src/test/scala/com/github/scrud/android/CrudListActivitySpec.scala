package com.github.scrud.android

import _root_.android.content.Intent
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.tester.android.view.TestMenu
import org.scalatest.matchers.MustMatchers
import android.view.{View, ContextMenu}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.{CrudApplication, EntityName}
import android.util.SparseArray
import com.github.scrud.state.State
import com.github.scrud.persistence.{CrudPersistence, PersistenceFactory}
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.action.CrudOperation

/** A test for [[com.github.scrud.android.CrudListActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudListActivitySpec extends MustMatchers with CrudMockitoSugar {
  @Test
  def mustBeConstructibleWithoutAnApplicationYet() {
    new CrudListActivity
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

    val activity = new MyCrudListActivity(application)
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
    val activity = new MyCrudListActivity(application) {
      override lazy val crudContext = new AndroidCrudContext(this, application) {
        /**
         * Handle the exception by communicating it to the user and developers.
         */
        override def reportError(throwable: Throwable) {
          throw throwable
        }
      }
      override val applicationState = new State {}
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
    val activity = new MyCrudListActivity(application) {
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
    val activity = new MyCrudListActivity(application) {
      override lazy val entityType = _crudType.entityType
    }
    //shouldn't do anything
    activity.onCreateContextMenu(contextMenu, ignoredView, ignoredMenuInfo)
  }

  lazy val sparseArrayWorking: Boolean = {
    val array = new SparseArray[String]()
    array.put(0, "hello")
    val working = array.get(0) == "hello"
    if (working) error("SparseArray is now working!  You must have upgraded to a robolectric version that supports it.  Delete this code.")
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
    class SomeCrudListActivity extends MyCrudListActivity(application) {
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
    val activity = new MyCrudListActivity(application) {
      override lazy val entityType = _crudType.entityType
    }
    // should do nothing
    activity.onListItemClick(null, null, -1, -1)
  }
}