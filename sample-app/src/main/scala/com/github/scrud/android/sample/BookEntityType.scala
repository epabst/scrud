package com.github.scrud.android.sample

import com.github.triangle.PortableField._
import com.github.scrud.android._
import persistence.CursorField._
import com.github.scrud.{EntityName, EntityType}
import persistence.PersistedType._
import java.util.Date
import ForeignKey._
import view.ViewField._
import view.{EntityView, EnumerationView}
import com.github.scrud.Validation._
import com.github.scrud.platform.PlatformDriver

object Book extends EntityName("Book")

class BookEntityType(platformDriver: PlatformDriver) extends EntityType(Book, platformDriver) {
  val valueFields = List(
    foreignKey[AuthorEntityType](Author),

    persisted[String]("name") + namedViewField("name", textView) + requiredString,

    persisted[Int]("edition") + namedViewField("edition", intView),

    persistedEnum[Genre.Value]("genre", Genre) + namedViewField("genre", EnumerationView[Genre.Value](Genre)) +
      default(Genre.Fantasy),

    foreignKey[PublisherEntityType](Publisher) + namedViewField("publisher", EntityView(Publisher)),

    persistedDate("publishDate") + namedViewField[Date]("publishDate", dateView)
  )
}
