package com.github.scrud.android.sample

import com.github.scrud
import scrud.android._
import scrud.android.persistence.CursorField._
import scrud.android.persistence.EntityType
import scrud.android.view.ViewField._
import com.github.triangle._
import scrud.android.validate.Validation._

object PublisherEntityType extends EntityType {
  def entityName = "Publisher"

  def valueFields = List(
    persisted[String]("name") + viewId(classOf[R], "publisher_name", textView) + requiredString,

    viewId(classOf[R], "bookCount", intView) + bundleField[Int]("bookCount") + GetterFromItem[Int] {
      case UriField(Some(uri)) && CrudContextField(Some(crudContext)) => {
        println("calculating bookCount for " + uri + " and " + crudContext)
        crudContext.withEntityPersistence(BookEntityType) { persistence =>
          val books = persistence.findAll(uri)
          Some(books.size)
        }
      }
    }
  )
}
