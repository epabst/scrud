package com.github.scrud.context

import org.scalatest.FunSpec
import org.mockito.Mockito._
import com.github.scrud.persistence.{EntityTypeMapForTesting, PersistenceFactoryForTesting, ThinPersistence}
import org.scalatest.mock.MockitoSugar
import com.github.scrud.{UriPath, EntityTypeForTesting}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * A specification for [[com.github.scrud.context.SharedContext]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/11/13
 * Time: 4:06 PM
 */
@RunWith(classOf[JUnitRunner])
class SharedContextSpec extends FunSpec with MockitoSugar {
  describe("withPersistence") {
    it("must close persistence") {
      val entityType = EntityTypeForTesting
      val thinPersistence = mock[ThinPersistence]
      val persistenceFactory = new PersistenceFactoryForTesting(thinPersistence)
      val sharedContext = new SimpleSharedContext(EntityTypeMapForTesting(entityType -> persistenceFactory))
      sharedContext.withPersistence { p => p.persistenceFor(entityType).findAll(UriPath.EMPTY) }
      verify(thinPersistence).close()
    }

    it("must close persistence on failure") {
      val entityType = EntityTypeForTesting
      val thinPersistence = mock[ThinPersistence]
      val persistenceFactory = new PersistenceFactoryForTesting(thinPersistence)
      val sharedContext = new SimpleSharedContext(EntityTypeMapForTesting(entityType -> persistenceFactory))
      try {
        sharedContext.withPersistence { persistenceConnection =>
          persistenceConnection.persistenceFor(entityType)
          throw new IllegalArgumentException("intentional")
        }
        fail("should have propagated exception")
      } catch {
        case e: IllegalArgumentException => "expected"
      }
      verify(thinPersistence).close()
    }
  }
}
