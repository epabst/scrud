package com.github.scrud

import com.github.scrud.platform.TestingPlatformDriver

/**
 * An EntityType for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 3:52 PM
 */
class EntityTypeForTesting(entityName: EntityName = EntityName("MyEntity")) extends EntityType(entityName, TestingPlatformDriver)

object EntityTypeForTesting extends EntityTypeForTesting(EntityName("MyEntity"))
