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

//todo delete this
object BookEntityType extends BookEntityType(new AndroidPlatformDriver(classOf[R]))

class BookEntityType(platformDriver: PlatformDriver) extends EntityType(Book, platformDriver) {
  val valueFields = List(
    foreignKey(Author),

    persisted[String]("name") + viewId(classOf[R], "name", textView) + requiredString,

    persisted[Int]("edition") + viewId(classOf[R], "edition", intView),

    persistedEnum[Genre.Value]("genre", Genre) + viewId(classOf[R], "genre", EnumerationView[Genre.Value](Genre)) +
      default(Genre.Fantasy),

    foreignKey(Publisher) + viewId(classOf[R], "publisher", EntityView(Publisher)),

    persistedDate("publishDate") + viewId[Date](classOf[R], "publishDate", dateView)
  )
}
