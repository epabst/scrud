package com.github.scrud.android

import com.github.scrud.{EntityName, UriPath, EntityType}
import com.github.triangle._
import view.ViewField._
import persistence.CursorField._
import res.R
import com.github.scrud.Validation._
import com.github.scrud.PlatformIndependentField._
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}

object EntityForTesting extends EntityName("MyMap")

/** An EntityType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

class EntityTypeForTesting(entityName: EntityName = EntityForTesting, platformDriver: PlatformDriver = TestingPlatformDriver)
    extends EntityType(entityName, platformDriver) {
  def valueFields = List[BaseField](
    persisted[String]("name") + viewId(R.id.name, textView) + requiredString + loadingIndicator("..."),
    persisted[Int]("age") + viewId(R.id.age, intView),
    //here to test a non-UI field
    persisted[String]("uri") + Getter[UriPath,String](u => Some(u.toString)))
}

object EntityTypeForTesting extends EntityTypeForTesting(EntityForTesting, TestingPlatformDriver)
