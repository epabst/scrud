package com.github.scrud.context

import org.scalatest.FunSpec
import org.mockito.Mockito._
import com.github.scrud.persistence.{PersistenceFactoryForTesting, ThinPersistence}
import org.scalatest.mock.MockitoSugar
import com.github.scrud.{UriPath, EntityTypeForTesting}
import com.github.scrud.platform.TestingPlatformDriver
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
      val persistence = mock[ThinPersistence]
      val entityTypeMap = new PersistenceFactoryForTesting(entityType, persistence).toEntityTypeMap
      val sharedContext = new SimpleSharedContext(entityTypeMap, TestingPlatformDriver)
      sharedContext.withPersistence { p => p.persistenceFor(entityType).findAll(UriPath.EMPTY) }
      verify(persistence).close()
    }

    it("must close persistence on failure") {
      val entityType = EntityTypeForTesting
      val persistence = mock[ThinPersistence]
      val entityTypeMap = new PersistenceFactoryForTesting(entityType, persistence).toEntityTypeMap
      val sharedContext = new SimpleSharedContext(entityTypeMap, TestingPlatformDriver)
      try {
        sharedContext.withPersistence { p =>
          p.persistenceFor(entityType)
          throw new IllegalArgumentException("intentional")
        }
        fail("should have propagated exception")
      } catch {
        case e: IllegalArgumentException => "expected"
      }
      verify(persistence).close()
    }
  }
}