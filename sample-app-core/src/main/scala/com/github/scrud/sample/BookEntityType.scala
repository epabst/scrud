package com.github.scrud.sample

import com.github.scrud.EntityType
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{TitleQT, PositiveIntQT, DateWithoutTimeQT}
import com.github.scrud.platform.representation._
import com.github.scrud.copy.types.{Validation, Default}
import com.github.scrud.sample.Genre._
import com.github.scrud.platform.representation.DisplayUI
import com.github.scrud.types.EnumerationValueQT
import com.github.scrud.EntityName

object Book extends EntityName("Book")

class BookEntityType(platformDriver: PlatformDriver) extends EntityType(Book, platformDriver) {
  val nameField = field("name", TitleQT, Seq(Persistence(1), EditUI, DisplayUI(FieldLevel.Identity), Validation.requiredString))

  val author = field(Author, Seq(Persistence(1), EditUI, DisplayUI(FieldLevel.Identity)))

  field("edition", PositiveIntQT, Seq(Persistence(1), EditUI, DisplayUI(FieldLevel.Detail)))

  val genre = field(EnumerationValueQT[Genre](Genre), Seq(Persistence(1), EditUI, DisplayUI(FieldLevel.Detail), Default(Fantasy)))

  field(Publisher, Seq(Persistence(2), EditUI, DisplayUI(FieldLevel.Detail)))

  field("publishDate", DateWithoutTimeQT, Seq(Persistence(1), EditUI, DisplayUI(FieldLevel.Detail)))
}
