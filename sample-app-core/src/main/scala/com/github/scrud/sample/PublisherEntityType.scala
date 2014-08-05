package com.github.scrud.sample

import com.github.scrud
import scrud.platform.PlatformDriver
import com.github.scrud.types.{NaturalIntQT, TitleQT}
import scrud.EntityType
import com.github.scrud.platform.representation._
import com.github.scrud.copy.types.Validation
import com.github.scrud.copy.Calculation
import com.github.scrud.platform.representation.DisplayUI
import com.github.scrud.EntityName
import scala.Some

object Publisher extends EntityName("Publisher")

class PublisherEntityType(platformDriver: PlatformDriver) extends EntityType(Publisher, platformDriver) {
  field("name", TitleQT, Seq(Persistence(2), EditUI, DisplayUI(FieldLevel.Identity), Validation.requiredString))

  field("bookCount", NaturalIntQT, Seq(DisplayUI(FieldLevel.Summary),
    Calculation { context => Some(context.findAll(Book).size) }))
}
