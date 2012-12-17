package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import org.mockito.Matchers
import com.github.scrud.state.State
import com.github.scrud.android.MyEntityType
import com.github.scrud.util.{CrudMockitoSugar, ListenerHolder}
import com.github.scrud.{CrudContext, UriPath, EntityType}

/** A specification for [[com.github.scrud.persistence.DerivedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class DerivedPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

  it("must instantiate the CrudPersistence for the delegate CrudTypes and make them available") {
    val entityType1 = new MyEntityType
    val entityType2 = new MyEntityType
    val persistence1 = mock[CrudPersistence]
    val persistence2 = mock[CrudPersistence]
    val factory = new DerivedPersistenceFactory[String](entityType1, entityType2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityType, CrudPersistence]) = {
        delegatePersistenceMap must be (Map(entityType1 -> persistence1, entityType2 -> persistence2))
        List("findAll", "was", "called")
      }
    }
    val crudContext = mock[CrudContext]
    stub(crudContext.applicationState).toReturn(new State {})
    stub(crudContext.dataListenerHolder(Matchers.any())).toReturn(dataListenerHolder)
    when(crudContext.openEntityPersistence(entityType1)).thenReturn(persistence1)
    when(crudContext.openEntityPersistence(entityType2)).thenReturn(persistence2)
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.findAll(UriPath()) must be (List("findAll", "was", "called"))
  }

  it("must close each delegate CrudPersistence when close is called") {
    val entityType1 = mock[EntityType]
    val entityType2 = mock[EntityType]
    val factory = new DerivedPersistenceFactory[String](entityType1, entityType2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityType, CrudPersistence]) = Nil
    }
    val crudContext = mock[CrudContext]
    stub(crudContext.applicationState).toReturn(new State {})
    stub(crudContext.dataListenerHolder(Matchers.any())).toReturn(dataListenerHolder)
    val persistence1 = mock[CrudPersistence]
    val persistence2 = mock[CrudPersistence]
    when(crudContext.openEntityPersistence(entityType1)).thenReturn(persistence1)
    when(crudContext.openEntityPersistence(entityType2)).thenReturn(persistence2)
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.close()
    verify(persistence1).close()
    verify(persistence2).close()
  }
}