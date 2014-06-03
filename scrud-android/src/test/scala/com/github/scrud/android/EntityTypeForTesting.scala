package com.github.scrud.android

import com.github.scrud._
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.types.{UriQT, NaturalIntQT, TitleQT}
import com.github.scrud.platform.representation.{EditUI, SummaryUI, SelectUI, Persistence}
import com.github.scrud.copy.types.Validation

object EntityForTesting extends EntityName("MyMap")

/** An EntityType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

class EntityTypeForTesting(entityName: EntityName = EntityForTesting, platformDriver: PlatformDriver = TestingPlatformDriver)
    extends EntityType(entityName, platformDriver) {
  val name = field("name", TitleQT, Seq(Persistence(1), EditUI, SelectUI, Validation.requiredString, LoadingIndicator("...")))

  val age = field("age", NaturalIntQT, Seq(Persistence(1), EditUI, SummaryUI))

  val url = field("url", UriQT, Seq(Persistence(1)))
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityForTesting, TestingPlatformDriver)
