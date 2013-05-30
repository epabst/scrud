package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud._
import com.github.scrud.EntityName
import com.github.scrud.android.{CrudTypeForTesting, CrudApplicationForTesting}

/** A specification for [[com.github.scrud.persistence.DerivedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class DerivedPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  it("must instantiate the CrudPersistence for the delegate CrudTypes and make them available") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val persistence1 = mock[ThinPersistence]
    val persistence2 = mock[ThinPersistence]
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = {
        delegatePersistenceMap.keySet must be (Set(entity1, entity2))
        delegatePersistenceMap.values.forall(_.entityType != null)
        List("findAll", "was", "called")
      }
    }
    val crudContext = SimpleCrudContext(new CrudApplicationForTesting(new CrudTypeForTesting(entity1, persistence1), new CrudTypeForTesting(entity2, persistence2)))
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.findAll(UriPath()) must be (List("findAll", "was", "called"))
  }

  it("must close each delegate CrudPersistence when close is called") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = Nil
    }
    val persistence1 = mock[ThinPersistence]
    val persistence2 = mock[ThinPersistence]
    val crudContext = SimpleCrudContext(new CrudApplicationForTesting(new CrudTypeForTesting(entity1, persistence1), new CrudTypeForTesting(entity2, persistence2)))
    val persistence = factory.createEntityPersistence(mock[EntityType], crudContext)
    persistence.close()
    verify(persistence1).close()
    verify(persistence2).close()
  }
}