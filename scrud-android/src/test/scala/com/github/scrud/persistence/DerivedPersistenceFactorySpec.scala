package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import org.mockito.Matchers
import com.github.scrud.state.State
import com.github.scrud.util.{CrudMockitoSugar, ListenerHolder}
import com.github.scrud.{EntityName, CrudContext, UriPath, EntityType}

/** A specification for [[com.github.scrud.persistence.DerivedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class DerivedPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

  it("must instantiate the CrudPersistence for the delegate CrudTypes and make them available") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val persistence1 = mock[CrudPersistence]
    val persistence2 = mock[CrudPersistence]
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = {
        delegatePersistenceMap must be (Map(entity1 -> persistence1, entity2 -> persistence2))
        List("findAll", "was", "called")
      }
    }
    val crudContext = mock[CrudContext]
    stub(crudContext.applicationState).toReturn(new State)
    stub(crudContext.dataListenerHolder(Matchers.any[EntityName]())).toReturn(dataListenerHolder)
    when(crudContext.openEntityPersistence(entity1)).thenReturn(persistence1)
    when(crudContext.openEntityPersistence(entity2)).thenReturn(persistence2)
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.findAll(UriPath()) must be (List("findAll", "was", "called"))
  }

  it("must close each delegate CrudPersistence when close is called") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = Nil
    }
    val crudContext = mock[CrudContext]
    stub(crudContext.applicationState).toReturn(new State)
    stub(crudContext.dataListenerHolder(Matchers.any[EntityName]())).toReturn(dataListenerHolder)
    val persistence1 = mock[CrudPersistence]
    val persistence2 = mock[CrudPersistence]
    when(crudContext.openEntityPersistence(entity1)).thenReturn(persistence1)
    when(crudContext.openEntityPersistence(entity2)).thenReturn(persistence2)
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.close()
    verify(persistence1).close()
    verify(persistence2).close()
  }
}