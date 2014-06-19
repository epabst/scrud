package com.github.scrud.persistence

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.context.{CommandContextForTesting, SharedContextForTesting}
import com.github.scrud.EntityTypeForTesting
import com.github.scrud.copy.types.MapStorage
import org.mockito.Mockito

/**
 * A behavior specification for [[com.github.scrud.persistence.SingletonWithChangeLogPersistenceFactory]]
 * and [[com.github.scrud.persistence.SingletonWithChangeLogCrudPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 */
@RunWith(classOf[JUnitRunner])
class SingletonWithChangeLogPersistenceFactorySpec extends FunSpec with MustMatchers with CrudMockitoSugar {
  describe("save") {
    it("must notify listeners exactly once") {
      val persistenceFactory = PersistenceFactoryForTesting
      val persistenceFactory2 = new SingletonWithChangeLogPersistenceFactory(persistenceFactory)
      val listener: DataListener = mock[DataListener]
      val listener2: DataListener = mock[DataListener]
      val entityType = EntityTypeForTesting
      val entityTypeMap = new EntityTypeMapForTesting(entityType)
      val sharedContext: SharedContextForTesting = new SharedContextForTesting(entityTypeMap)
      persistenceFactory.addListener(listener, entityType, sharedContext)
      persistenceFactory2.addListener(listener2, entityType, sharedContext)

      val commandContext = new CommandContextForTesting(sharedContext)
      val persistence2 = persistenceFactory2.createEntityPersistence(entityType, new PersistenceConnection(commandContext))
      persistence2.save(None, MapStorage, new MapStorage(entityType.Name -> Some("George")), commandContext)
      Mockito.verify(listener, Mockito.times(1)).onChanged()
      Mockito.verify(listener2, Mockito.times(1)).onChanged()
    }
  }
}
