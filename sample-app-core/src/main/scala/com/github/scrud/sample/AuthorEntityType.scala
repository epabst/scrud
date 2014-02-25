package com.github.scrud.sample

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{NaturalIntQT, TitleQT}
import com.github.scrud.platform.representation.{SummaryUI, EditUI, Persistence}
import com.github.scrud.copy.types.Validation
import com.github.scrud.copy.Calculation

object Author extends EntityName("Author")

class AuthorEntityType(platformDriver: PlatformDriver) extends EntityType(Author, platformDriver) {
  val nameField = field("name", TitleQT, Seq(Persistence(1), EditUI, Validation.requiredString))

  val bookCount = field("bookCount", NaturalIntQT, Seq(SummaryUI,
    Calculation { requestContext => Some(requestContext.findAll(Book).size) }))
}
