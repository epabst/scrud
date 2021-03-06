package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud._
import com.github.scrud.context._
import com.github.scrud.EntityName
import org.scalatest.junit.JUnitRunner

/** A specification for [[com.github.scrud.persistence.DerivedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class DerivedPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  it("must make the CrudPersistence for the delegate EntityNames available") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, persistenceConnection: PersistenceConnection) = {
        persistenceConnection.persistenceFor(entity1) must not be null
        persistenceConnection.persistenceFor(entity2) must not be null
        List("findAll", "was", "called")
      }
    }
    val entityType1 = new EntityTypeForTesting(entity1)
    val entityType2 = new EntityTypeForTesting(entity2)
    val derivedEntity = EntityName("derivedEntity")
    val derivedEntityType = new EntityTypeForTesting(derivedEntity)
    val entityTypeMap = EntityTypeMapForTesting(entityType1, entityType2, derivedEntityType)
    val commandContext = new CommandContextForTesting(entityTypeMap)
    val persistenceConnection = new PersistenceConnection(commandContext)
    val persistence = factory.createEntityPersistence(derivedEntityType, persistenceConnection)
    persistence.findAll(UriPath()) must be (List("findAll", "was", "called"))
  }

  it("must close each used delegate CrudPersistence when close is called") {
    val entity1 = EntityName("entity1")
    val entity2 = EntityName("entity2")
    val persistence1 = mock[ThinPersistence]
    val persistence2 = mock[ThinPersistence]
    val factory = new DerivedPersistenceFactory[String](entity1, entity2) {
      def findAll(entityType: EntityType, uri: UriPath, persistenceConnection: PersistenceConnection) = {
        persistenceConnection.persistenceFor(entity1) must not be null
        persistenceConnection.persistenceFor(entity2) must not be null
        Nil
      }
    }
    val derivedEntity = EntityName("derivedEntity")
    val derivedEntityType = new EntityTypeForTesting(derivedEntity)
    val entityTypeMap = EntityTypeMapForTesting(
      new EntityTypeForTesting(entity1) -> new PersistenceFactoryForTesting(persistence1),
      new EntityTypeForTesting(entity2) -> new PersistenceFactoryForTesting(persistence2),
      derivedEntityType -> factory)
    val commandContext = new CommandContextForTesting(entityTypeMap)
    val persistenceConnection = commandContext.persistenceConnection
    val derivedCrudPersistence = factory.createEntityPersistence(derivedEntityType, persistenceConnection)
    derivedCrudPersistence.findAll(UriPath.EMPTY)
    persistenceConnection.close()
    verify(persistence1).close()
    verify(persistence2).close()
  }
}
