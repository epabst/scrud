package com.github.scrud.sample

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{TitleQT, PositiveIntQT, EnumerationValueQT, DateWithoutTimeQT}
import com.github.scrud.platform.representation.{SelectUI, EditUI, Persistence}

object Book extends EntityName("Book")

class BookEntityType(platformDriver: PlatformDriver) extends EntityType(Book, platformDriver) {
  field("name", TitleQT, Seq(Persistence, EditUI, SelectUI)) //todo requiredString

  field("authorId", Author, Seq(Persistence, EditUI))

  field("edition", PositiveIntQT, Seq(Persistence, EditUI))

  field("genre", EnumerationValueQT[Genre.Value](Genre), Seq(Persistence, EditUI)) //todo default(Genre.Fantasy)

  field("publisherId", Publisher, Seq(Persistence, EditUI)) //todo Persistence(dataVersion = 2)

  field("publishDate", DateWithoutTimeQT, Seq(Persistence, EditUI))
}
