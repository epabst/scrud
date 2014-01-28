package com.github.scrud

import com.github.scrud.copy.{InstantiatingTargetType, SourceType}
import com.github.scrud.platform.TestingPlatformDriver

/**
 * An EntityType for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 3:52 PM
 */
class EntityTypeForTesting(entityName: EntityName = EntityName("MyEntity")) extends EntityType(entityName, TestingPlatformDriver) {
  def copyAndUpdate[T <: AnyRef](sourceType: SourceType, source: AnyRef, targetType: InstantiatingTargetType[T]) =
    throw new UnsupportedOperationException

  def findPersistedId(readable: AnyRef) = throw new UnsupportedOperationException
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityName("MyEntity"))
