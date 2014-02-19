package com.github.scrud.action

import org.scalatest.FunSpec
import com.github.scrud.persistence._
import org.mockito.Mockito._
import com.github.scrud.{EntityNavigationForTesting, EntityTypeForTesting, UriPath}
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.platform.TestingPlatformDriver
import org.scalatest.matchers.MustMatchers
import com.github.scrud.context.RequestContextForTesting
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.copy.types.MapStorage

/**
 * A behavior specification for [[com.github.scrud.action.StartEntityDeleteOperation]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/30/12
 * Time: 3:32 PM
 */
@RunWith(classOf[JUnitRunner])
class StartEntityDeleteOperationSpec extends FunSpec with CrudMockitoSugar with MustMatchers {
  val platformDriver = TestingPlatformDriver

  it("must delete with option to undo") {
    val entity = new EntityTypeForTesting
    val entityName = entity.entityName
    val readable = new MapStorage(entityName, entity.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entityName) / 345L
    val persistence = mock[ThinPersistence]
    stub(persistence.newWritable()).toReturn(new MapStorage)
    stub(persistence.findAll(uri)).toReturn(Seq(readable))
    var allowUndoCalled = false
    val persistenceFactory = new PersistenceFactoryForTesting(entity, persistence)
    val entityTypeMap = EntityTypeMapForTesting(persistenceFactory)
    val requestContext = new RequestContextForTesting(entityTypeMap) {
      override def allowUndo(undoable: Undoable) {
        allowUndoCalled = true
        undoable.closeOperation.foreach(_.invoke(uri, this))
      }
    }
    val actionToDelete = new EntityNavigationForTesting(entityTypeMap).actionToDelete(entityName).get
    actionToDelete.invoke(uri, requestContext)
    verify(persistence).delete(uri)
    allowUndoCalled must be (true)
  }

  it("undo must work") {
    val entity = new EntityTypeForTesting
    val entityName = entity.entityName
    val readable = new MapStorage(entityName, entity.idFieldName -> Some(345L), "name" -> Some("George"))
    val uri = UriPath(entityName) / 345L
    val thinPersistence = mock[ThinPersistence]
    stub(thinPersistence.findAll(uri)).toReturn(Seq(readable))
    stub(thinPersistence.newWritable()).toReturn(new MapStorage)
    val persistenceFactory = new PersistenceFactoryForTesting(entity, thinPersistence)
    val entityTypeMap = EntityTypeMapForTesting(persistenceFactory)
    val requestContext = new RequestContextForTesting(entityTypeMap) {
      override def allowUndo(undoable: Undoable) {
        undoable.undoAction.invoke(uri, this)
      }
    }
    val operation = new StartEntityDeleteOperation(entity)
    operation.invoke(uri, new PersistenceConnection(entityTypeMap, requestContext.sharedContext), requestContext)
    verify(thinPersistence).delete(uri)
    verify(thinPersistence).save(Some(345L), new MapStorage(entityName, entity.idFieldName -> Some(345L), "name" -> Some("George")))
  }
}
