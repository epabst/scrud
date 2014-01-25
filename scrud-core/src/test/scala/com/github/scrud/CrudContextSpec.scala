package com.github.scrud

import android.{CrudApplicationForTesting, CrudTypeForTesting}
import org.scalatest.FunSpec
import org.mockito.Mockito._
import com.github.scrud.persistence.ThinPersistence
import org.scalatest.mock.MockitoSugar

/**
 * A specification for [[com.github.scrud.CrudContext]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/11/13
 * Time: 4:06 PM
 */
class CrudContextSpec extends FunSpec with MockitoSugar {
  describe("withPersistence") {
    it("must close persistence") {
      val persistence = mock[ThinPersistence]
      val _crudType = new CrudTypeForTesting(persistence)
      val application = new CrudApplicationForTesting(_crudType)
      val crudContext = new SimpleCrudContext(application)
      crudContext.withEntityPersistence(_crudType.entityType) { p => p.findAll(UriPath.EMPTY) }
      verify(persistence).close()
    }

    it("must close persistence on failure") {
      val persistence = mock[ThinPersistence]
      val _crudType = new CrudTypeForTesting(persistence)
      val application = new CrudApplicationForTesting(_crudType)
      val crudContext = new SimpleCrudContext(application)
      try {
        crudContext.withEntityPersistence(_crudType.entityType) { p => throw new IllegalArgumentException("intentional") }
        fail("should have propogated exception")
      } catch {
        case e: IllegalArgumentException => "expected"
      }
      verify(persistence).close()
    }
  }
}
