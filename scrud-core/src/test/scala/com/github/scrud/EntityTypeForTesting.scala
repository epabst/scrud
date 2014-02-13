package com.github.scrud

import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.types.TitleQT
import com.github.scrud.platform.representation.{Persistence}
import com.github.scrud.copy.types.MapStorage

/**
 * An EntityType for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 3:52 PM
 */
class EntityTypeForTesting(entityName: EntityName = EntityName("MyEntity")) extends EntityType(entityName, TestingPlatformDriver) {
  field("name", TitleQT, Seq(MapStorage, Persistence(1)))
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityName("MyEntity"))
