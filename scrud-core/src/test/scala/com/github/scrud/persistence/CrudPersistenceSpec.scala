package com.github.scrud.persistence

import org.scalatest.{MustMatchers, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.EntityTypeForTesting
import com.github.scrud.context.CommandContextForTesting
import com.github.scrud.copy.types.MapStorage

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/1/14
 */
@RunWith(classOf[JUnitRunner])
class CrudPersistenceSpec extends FunSpec with MustMatchers {
  describe("toWritable") {
    it("must return the source if the sourceType equals the writableType") {
      val entityType = EntityTypeForTesting
      val commandContext = new CommandContextForTesting(entityType)
      val persistence = commandContext.persistenceFor(entityType)
      val source = new MapStorage(entityType.name -> Some("George"))
      val writable = persistence.toWritable(persistence.writableType, source, entityType.toUri, commandContext)
      (writable eq source) must be (true)
    }
  }
}
