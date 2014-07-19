package com.github.scrud

import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.types.{DateWithoutTimeQT, TitleQT}
import com.github.scrud.platform.representation._
import com.github.scrud.copy.types.{Validation, MapStorage}

/**
 * An EntityType for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 3:52 PM
 */
class EntityTypeForTesting(entityName: EntityName = EntityName("MyEntity")) extends EntityType(entityName, TestingPlatformDriver) {
  val name = field("name", TitleQT, Seq(MapStorage, Persistence(1), EditUI, SelectUI, Query, Validation.requiredString, LoadingIndicator("...")))
  val birthDate = field("birthDate", DateWithoutTimeQT, Seq(MapStorage, Persistence(1), EditUI, SelectUI, Query))
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityName("MyEntity"))
