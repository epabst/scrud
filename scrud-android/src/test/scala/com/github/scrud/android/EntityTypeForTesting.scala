package com.github.scrud.android

import com.github.scrud._
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.types.{UriQT, NaturalIntQT, TitleQT}
import com.github.scrud.platform.representation._
import com.github.scrud.copy.types.Validation
import com.github.scrud.android.testres.R
import com.github.scrud.EntityName

object EntityForTesting extends EntityName("MyMap")

/** An EntityType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

class EntityTypeForTesting(entityName: EntityName = EntityForTesting, platformDriver: PlatformDriver = AndroidPlatformDriverForTesting)
    extends EntityType(entityName, platformDriver) {
  val name = field("name", TitleQT, Seq(Persistence(1), EditUI, SelectUI, Validation.requiredString, Query, LoadingIndicator("...")))

  val age = field("age", NaturalIntQT, Seq(Persistence(1), EditUI, SummaryUI, Query))

  val url = field("url", UriQT, Seq(Persistence(1), Query))

  val parent = field("parent", EntityForTesting, Seq(Persistence(1), DetailUI, EditUI))
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityForTesting, AndroidPlatformDriverForTesting)
