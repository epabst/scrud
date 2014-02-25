package com.github.scrud.sample

import com.github.scrud
import scrud.platform.PlatformDriver
import com.github.scrud.types.{NaturalIntQT, TitleQT}
import scrud.{EntityName, EntityType}
import com.github.scrud.platform.representation.{SummaryUI, SelectUI, Persistence, EditUI}
import com.github.scrud.copy.types.Validation
import com.github.scrud.copy.Calculation

object Publisher extends EntityName("Publisher")

class PublisherEntityType(platformDriver: PlatformDriver) extends EntityType(Publisher, platformDriver) {
  field("name", TitleQT, Seq(Persistence(2), EditUI, SelectUI, Validation.requiredString))

  field("bookCount", NaturalIntQT, Seq(SummaryUI,
    Calculation { requestContext => Some(requestContext.findAll(Book).size) }))
}
