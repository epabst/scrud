package com.github.scrud.android

import action.AndroidOperation
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.RobolectricTestRunner
import persistence.CursorField
import scala.collection.mutable
import org.scalatest.matchers.MustMatchers
import AndroidOperation._
import android.widget.ListAdapter
import com.github.scrud.UriPath
import org.mockito.Mockito._
import com.github.scrud.persistence._
import com.github.scrud.util.{CrudMockitoSugar, ReadyFuture}
import scala.Some

/** A test for [[com.github.scrud.android.CrudListActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[RobolectricTestRunner])
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
}