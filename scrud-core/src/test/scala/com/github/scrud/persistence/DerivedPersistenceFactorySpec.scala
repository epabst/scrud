package com.github.scrud.persistence

//import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud._
import com.github.scrud.context._
import com.github.scrud.EntityName

/** A specification for [[com.github.scrud.persistence.DerivedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
//@RunWith(classOf[JUnitRunner])
class DerivedPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  it("must instantiate the CrudPersistence for the delegate CrudTypes and make them available") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val persistence1 = mock[ThinPersistence]
    val persistence2 = mock[ThinPersistence]
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, sharedContext: SharedContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = {
        delegatePersistenceMap.keySet must be (Set(entity1, entity2))
        delegatePersistenceMap.values.forall(_.entityType != null)
        List("findAll", "was", "called")
      }
    }
    val persistenceFactory1 = new PersistenceFactoryForTesting(new EntityTypeForTesting(entity1), persistence1)
    val persistenceFactory2 = new PersistenceFactoryForTesting(new EntityTypeForTesting(entity2), persistence2)
    val sharedContext = new SharedContextForTesting(new EntityTypeMap(persistenceFactory1.toTuple, persistenceFactory2.toTuple))
    val persistence = factory.createEntityPersistence(mock[EntityType], sharedContext)
    persistence.findAll(UriPath()) must be (List("findAll", "was", "called"))
  }

  it("must close each delegate CrudPersistence when close is called") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, sharedContext: SharedContext, delegatePersistenceMap: Map[EntityName, CrudPersistence]) = Nil
    }
    val persistence1 = mock[ThinPersistence]
    val persistence2 = mock[ThinPersistence]
    val sharedContext = new SharedContextForTesting(EntityTypeMap(new PersistenceFactoryForTesting(new EntityTypeForTesting(entity1), persistence1).toTuple,
      new PersistenceFactoryForTesting(new EntityTypeForTesting(entity2), persistence2).toTuple))
    val persistence = factory.createEntityPersistence(mock[EntityType], sharedContext)
    persistence.close()
    verify(persistence1).close()
    verify(persistence2).close()
  }
}