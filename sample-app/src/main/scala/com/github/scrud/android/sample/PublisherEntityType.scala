package com.github.scrud.android.sample

import com.github.scrud
import scrud.android.persistence.CursorField._
import scrud.{CrudContextField, EntityName, UriField, EntityType}
import scrud.android.view.ViewField._
import com.github.triangle._
import scrud.Validation._

object Publisher extends EntityName("Publisher")

object PublisherEntityType extends PublisherEntityType
class PublisherEntityType extends EntityType(Publisher) {
  val valueFields = List(
    persisted[String]("name") + viewId(classOf[R], "publisher_name", textView) + requiredString,

    viewId(classOf[R], "bookCount", intView) + bundleField[Int]("bookCount") + Getter[Int] {
      case UriField(Some(uri)) && CrudContextField(Some(crudContext)) => {
        println("calculating bookCount for " + uri + " and " + crudContext)
        crudContext.withEntityPersistence(Book) { persistence =>
          val books = persistence.findAll(uri)
          Some(books.size)
        }
      }
    }
  )
}
