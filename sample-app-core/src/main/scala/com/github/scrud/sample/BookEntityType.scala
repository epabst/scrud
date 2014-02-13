package com.github.scrud.sample

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{TitleQT, PositiveIntQT, EnumerationValueQT, DateWithoutTimeQT}
import com.github.scrud.platform.representation.{SelectUI, EditUI, Persistence}
import com.github.scrud.copy.types.Default

object Book extends EntityName("Book")

class BookEntityType(platformDriver: PlatformDriver) extends EntityType(Book, platformDriver) {
  field("name", TitleQT, Seq(Persistence(1), EditUI, SelectUI)) //todo requiredString

  field(Author, Seq(Persistence(1), EditUI))

  field("edition", PositiveIntQT, Seq(Persistence(1), EditUI))

  val genre = field("genre", EnumerationValueQT[Genre.Value](Genre), Seq(Persistence(1), EditUI), Default(Genre.Fantasy))

  field(Publisher, Seq(Persistence(2), EditUI))

  field("publishDate", DateWithoutTimeQT, Seq(Persistence(1), EditUI))
}