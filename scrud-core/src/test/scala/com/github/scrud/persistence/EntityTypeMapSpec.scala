package com.github.scrud.persistence

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.{EntityName, EntityTypeForTesting}

/**
 * A behavior specification for [[com.github.scrud.persistence.EntityTypeMap]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 9:42 AM
 */
class EntityTypeMapSpec extends FunSpec with MustMatchers {
  it("must enforce unique EntityNames") {
    val entityType1 = new EntityTypeForTesting(EntityName("Entity1"))
    val entityType2 = new EntityTypeForTesting(EntityName("Entity2"))
    val message = intercept[IllegalArgumentException] {
      EntityTypeMap(
        entityType1 -> ListBufferPersistenceFactoryForTesting,
        entityType2 -> ListBufferPersistenceFactoryForTesting,
        entityType1 -> ListBufferPersistenceFactoryForTesting)
    }.getMessage
    message must include ("unique")
    message must include ("Entity1")
    message must not include "Entity2"
  }
}
