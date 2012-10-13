package com.github.scrud.android

import common.UriPath
import com.github.triangle._
import entity.EntityName
import persistence.EntityType
import view.ViewField._
import persistence.CursorField._
import res.R
import validate.Validation._
import com.github.scrud.PlatformIndependentField._

object MyEntity extends EntityName("MyMap")

/** An EntityType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

class MyEntityType(entityName: EntityName = MyEntity) extends EntityType(entityName) {
  def valueFields = List[BaseField](
    persisted[String]("name") + viewId(R.id.name, textView) + requiredString + loadingIndicator("..."),
    persisted[Int]("age") + viewId(R.id.age, intView),
    //here to test a non-UI field
    persisted[String]("uri") + Getter[UriPath,String](u => Some(u.toString)))
}

object MyEntityType extends MyEntityType(MyEntity)
