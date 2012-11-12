package com.github.scrud.android.sample

import com.github.scrud.android._
import persistence.CursorField._
import com.github.scrud.{EntityName, EntityType}
import persistence.PersistedType._
import java.util.Date
import ForeignKey._
import view.ViewField._
import view.{EntityView, EnumerationView}
import com.github.scrud.Validation._

object Book extends EntityName("Book")

object BookEntityType extends EntityType(Book) {
  val valueFields = List(
    foreignKey(AuthorEntityType),

    persisted[String]("name") + viewId(classOf[R], "name", textView) + requiredString,

    persisted[Int]("edition") + viewId(classOf[R], "edition", intView),

    persistedEnum[Genre.Value]("genre", Genre) + viewId(classOf[R], "genre", EnumerationView[Genre.Value](Genre)),

    foreignKey(PublisherEntityType) + viewId(classOf[R], "publisher", EntityView(PublisherEntityType)),

    persistedDate("publishDate") + viewId[Date](classOf[R], "publishDate", dateView)
  )
}
