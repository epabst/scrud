package com.github.scrud.android.sample

import com.github.scrud.android._
import persistence.CursorField._
import persistence.EntityType
import view.ViewField._
import com.github.triangle._
import com.github.scrud.android.validate.Validation._

object AuthorEntityType extends EntityType {
  def entityName = "Author"

  def valueFields = List(
    persisted[String]("name") + viewId(classOf[R], "name", textView) + requiredString,

    viewId(classOf[R], "bookCount", intView) +
            bundleField[Int]("bookCount") +
            GetterFromItem[Int] {
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