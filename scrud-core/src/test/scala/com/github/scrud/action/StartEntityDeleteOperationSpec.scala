package com.github.scrud.action

import org.scalatest.FunSpec
import com.github.scrud.persistence.{ThinPersistence, CrudPersistenceUsingThin}
import org.mockito.Mockito._
import collection.mutable
import com.github.scrud.android.persistence.CursorField
import com.github.scrud.{SimpleCrudContext, UriPath}
import scala.Some
import com.github.scrud.android.{CrudTypeForTesting, EntityTypeForTesting, CrudApplicationForTesting}
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.platform.TestingPlatformDriver
import org.scalatest.matchers.MustMatchers

/**
 * A behavior specification for [[com.github.scrud.action.StartEntityDeleteOperation]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/30/12
 * Time: 3:32 PM
 */
class StartEntityDeleteOperationSpec extends FunSpec with CrudMockitoSugar with MustMatchers {
  val platformDriver = TestingPlatformDriver

  it("must delete with option to undo") {
    val entity = new EntityTypeForTesting
    val readable = mutable.Map[String,Option[Any]](CursorField.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entity.entityName) / 345L
    val persistence = mock[ThinPersistence]
    stub(persistence.newWritable()).toReturn(Map.empty)
    stub(persistence.findAll(uri)).toReturn(Seq(readable))
    val application = new CrudApplicationForTesting(platformDriver, new CrudTypeForTesting(entity, persistence))
    var allowUndoCalled = false
    val crudContext = new SimpleCrudContext(application) {
      override def allowUndo(undoable: Undoable) {
        allowUndoCalled = true
        undoable.closeOperation.foreach(_.invoke(uri, this))
      }
    }
    val actionToDelete = application.actionToDelete(entity).get
    actionToDelete.invoke(uri, crudContext)
    verify(persistence).delete(uri)
    allowUndoCalled must be (true)
  }

  it("undo must work") {
    val entity = new EntityTypeForTesting
    val readable = mutable.Map[String,Option[Any]](CursorField.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entity.entityName) / 345L
    val thinPersistence = mock[ThinPersistence]
    val crudPersistence = new CrudPersistenceUsingThin(entity, thinPersistence)
    stub(thinPersistence.findAll(uri)).toReturn(Seq(readable))
    stub(thinPersistence.newWritable()).toReturn(mutable.Map.empty[String,Option[Any]])
    val application = new CrudApplicationForTesting(platformDriver, new CrudTypeForTesting(entity, crudPersistence))
    val crudContext = new SimpleCrudContext(application) {
      override def allowUndo(undoable: Undoable) {
        undoable.undoAction.invoke(uri, this)
      }
    }
    val operation = new StartEntityDeleteOperation(entity)
    operation.invoke(uri, crudPersistence, crudContext)
    verify(thinPersistence).delete(uri)
    verify(thinPersistence).save(Some(345L), mutable.Map(CursorField.idFieldName -> Some(345L), "name" -> Some("George")))
  }
}
