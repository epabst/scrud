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
import java.lang.IllegalStateException
import com.github.scrud.UriPath
import org.mockito.Mockito._
import actors.Future
import com.github.scrud.persistence._
import com.github.scrud.util.{CrudMockitoSugar, ReadyFuture}
import com.github.scrud.state.State
import org.mockito.Matchers._
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
    val entity = Map[String,Any]("name" -> "Bob", "age" -> 25)
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application

      override lazy val currentAction = UpdateActionName
      override def currentUriPath = uri
      override def future[T](body: => T) = new ReadyFuture[T](body)
    }
    activity.onCreate(null)
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    verify(persistence).save(None, Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> uri.toString))
    verify(persistence, never()).find(uri)
  }

  @Test
  def shouldAddIfIdNotFound() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Any]("name" -> "Bob", "age" -> 25)
    val uri = UriPath(_crudType.entityType.entityName)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application

      override lazy val currentAction = UpdateActionName
      override def currentUriPath = uri
      override def future[T](body: => T) = new ReadyFuture[T](body)
    }
    when(persistence.find(uri)).thenReturn(None)
    activity.onCreate(null)
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    verify(persistence).save(None, mutable.Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> uri.toString))
  }

  @Test
  def shouldAllowUpdating() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Any]("name" -> "Bob", "age" -> 25)
    val uri = UriPath(_crudType.entityType.entityName) / 101
    stub(persistence.find(uri)).toReturn(Some(entity))
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application

      override lazy val currentAction = UpdateActionName
      override lazy val currentUriPath = uri
      override def future[T](body: => T) = new ReadyFuture[T](body)
    }
    activity.onCreate(null)
    val viewData = _crudType.entityType.copyAndUpdate(activity, mutable.Map[String,Any]())
    viewData.get("name") must be (Some("Bob"))
    viewData.get("age") must be (Some(25))

    activity.onBackPressed()
    verify(persistence).save(Some(101),
      mutable.Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> uri.toString, CursorField.idFieldName -> 101))
  }

  @Test
  def withPersistenceShouldClosePersistence() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application
    }
    activity.crudContext.withEntityPersistence(_crudType.entityType) { p => p.findAll(UriPath.EMPTY) }
    verify(persistence).close()
  }

  @Test
  def withPersistenceShouldClosePersistenceWithFailure() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application
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
  def shouldHandleAnyExceptionWhenSaving() {
    stub(persistence.save(None, "unsaveable data")).toThrow(new IllegalStateException("intentional"))
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application
    }
    //should not throw an exception
    activity.saveBasedOnUserAction(persistence, "unsaveable data")
  }

  @Test
  def onPauseShouldNotCreateANewIdEveryTime() {
    val _crudType = new MyCrudType(persistence)
    val application = MyCrudApplication(_crudType)
    val entity = mutable.Map[String,Any]("name" -> "Bob", "age" -> 25)
    val uri = UriPath(_crudType.entityType.entityName)
    when(persistence.find(uri)).thenReturn(None)
    stub(persistence.save(None, mutable.Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> uri.toString))).toReturn(101)
    val activity = new CrudActivity {
      override lazy val entityType = _crudType.entityType
      override def crudApplication = application

      override def future[T](body: => T): Future[T] = new ReadyFuture[T](body)
    }
    activity.setIntent(constructIntent(AndroidOperation.CreateActionName, uri, activity, null))
    activity.onCreate(null)
    //simulate a user entering data
    _crudType.entityType.copy(entity, activity)
    activity.onBackPressed()
    activity.onBackPressed()
    verify(persistence, times(1)).save(None, mutable.Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> uri.toString))
    //all but the first time should provide an id
    verify(persistence).save(Some(101), mutable.Map[String,Any]("name" -> "Bob", "age" -> 25, "uri" -> (uri / 101).toString,
      CursorField.idFieldName -> 101))
  }

  @Test
  def itMustDeleteWithUndoPossibilityWhichMustBeClosable() {
    val persistence = mock[CrudPersistence]
    val activity = mock[CrudActivity]
    val crudContext = mock[CrudContext]
    stub(crudContext.activityState).toReturn(new State {})
    val entity = new MyEntityType
    val readable = mutable.Map[String,Any]()
    val uri = UriPath(entity.entityName) / 345L
    stub(activity.crudContext).toReturn(crudContext)
    stub(persistence.crudContext).toReturn(crudContext)
    stub(persistence.find(uri)).toReturn(Some(readable))
    stub(activity.allowUndo(notNull.asInstanceOf[Undoable])).toAnswer(answerWithInvocation { invocationOnMock =>
      val currentArguments = invocationOnMock.getArguments
      val undoable = currentArguments(0).asInstanceOf[Undoable]
      undoable.closeOperation.foreach(_.invoke(uri, activity))
    })
    new CrudActivity().startDelete(entity, uri, activity)
    verify(persistence).delete(uri)
  }

  @Test
  def undoOfDeleteMustWork() {
    val persistence = mock[CrudPersistence]
    val activity = mock[CrudActivity]
    val crudContext = mock[CrudContext]
    val entity = MyEntityType
    val readable = mutable.Map[String,Any](CursorField.idFieldName -> 345L, "name" -> "George")
    val uri = UriPath(entity.entityName) / 345L
    stub(activity.crudContext).toReturn(crudContext)
    val vars = new State {}
    stub(crudContext.activityState).toReturn(vars)
    stub(crudContext.activityContext).toReturn(activity)
    stub(crudContext.application).toReturn(MyCrudApplication(new MyCrudType(entity, persistence)))
    stub(activity.variables).toReturn(vars.variables)
    stub(persistence.crudContext).toReturn(crudContext)
    stub(persistence.find(uri)).toReturn(Some(readable))
    when(activity.allowUndo(notNull.asInstanceOf[Undoable])).thenAnswer(answerWithInvocation { invocationOnMock =>
      val currentArguments = invocationOnMock.getArguments
      val undoable = currentArguments(0).asInstanceOf[Undoable]
      undoable.undoAction.invoke(uri, activity)
    })
    new CrudActivity().startDelete(entity, uri, activity)
    verify(persistence).delete(uri)
    verify(persistence).save(Some(345L), mutable.Map(CursorField.idFieldName -> 345L, "name" -> "George"))
  }
}