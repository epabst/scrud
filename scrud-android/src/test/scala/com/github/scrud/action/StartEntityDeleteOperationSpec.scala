package com.github.scrud.action

import org.scalatest.FunSpec
import com.github.scrud.persistence.CrudPersistence
import org.mockito.Mockito._
import collection.mutable
import com.github.scrud.android.persistence.CursorField
import com.github.scrud.{CrudContext, UriPath}
import org.mockito.Matchers._
import scala.Some
import com.github.scrud.android.{MyCrudType, MyEntityType, MyCrudApplication}
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.platform.PlatformDriver

/**
 * A behavior specification for [[com.github.scrud.action.StartEntityDeleteOperation]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/30/12
 * Time: 3:32 PM
 */
class StartEntityDeleteOperationSpec extends FunSpec with CrudMockitoSugar {
  val crudContext = mock[CrudContext]
  val platformDriver = mock[PlatformDriver]

  it("must delete with option to undo") {
    val entity = new MyEntityType
    val readable = mutable.Map[String,Option[Any]](CursorField.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entity.entityName) / 345L
    val persistence = mock[CrudPersistence]
    stub(persistence.crudContext).toReturn(crudContext)
    stub(persistence.platformDriver).toReturn(platformDriver)
    stub(persistence.find(uri)).toReturn(Some(readable))
    stub(crudContext.allowUndo(notNull.asInstanceOf[Undoable])).toAnswer(answerWithInvocation { invocationOnMock =>
      val currentArguments = invocationOnMock.getArguments
      val undoable = currentArguments(0).asInstanceOf[Undoable]
      undoable.closeOperation.foreach(_.invoke(uri, crudContext))
    })
    val application = MyCrudApplication(new MyCrudType(entity, persistence))
    application.actionToDelete(entity).get.invoke(uri, crudContext)
    val operation = new StartEntityDeleteOperation(entity, application)
    operation.invoke(uri, persistence)
    verify(persistence).delete(uri)
  }

  it("undo must work") {
    val entity = new MyEntityType
    val readable = mutable.Map[String,Option[Any]](CursorField.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entity.entityName) / 345L
    val persistence = mock[CrudPersistence]
    stub(persistence.crudContext).toReturn(crudContext)
    stub(persistence.platformDriver).toReturn(platformDriver)
    stub(persistence.find(uri)).toReturn(Some(readable))
    stub(persistence.newWritable()).toReturn(mutable.Map.empty[String,Option[Any]])
    stub(crudContext.allowUndo(notNull.asInstanceOf[Undoable])).toAnswer(answerWithInvocation { invocationOnMock =>
      val currentArguments = invocationOnMock.getArguments
      val undoable = currentArguments(0).asInstanceOf[Undoable]
      undoable.undoAction.invoke(uri, crudContext)
    })
    stub(crudContext.withEntityPersistence(eql(entity))(any())).toAnswer(answerWithInvocation { invocationOnMock =>
      val currentArguments = invocationOnMock.getArguments
      val function = currentArguments(1).asInstanceOf[CrudPersistence => Any]
      function.apply(persistence)
    })
    val application = MyCrudApplication(new MyCrudType(entity, persistence))
    val operation = new StartEntityDeleteOperation(entity, application)
    operation.invoke(uri, persistence)
    verify(persistence).delete(uri)
    verify(persistence).save(Some(345L), mutable.Map(CursorField.idFieldName -> Some(345L), "name" -> Some("George")))
  }
}
