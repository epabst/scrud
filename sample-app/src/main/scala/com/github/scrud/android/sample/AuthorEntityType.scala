package com.github.scrud.android.sample

import com.github.scrud.android._
import persistence.CursorField._
import com.github.scrud.{CrudContextField, EntityName, UriField, EntityType}
import view.ViewField._
import com.github.triangle._
import com.github.scrud.Validation._
import com.github.scrud.platform.PlatformDriver

object Author extends EntityName("Author")

//todo delete this
object AuthorEntityType extends AuthorEntityType(new AndroidPlatformDriver(classOf[R]))

class AuthorEntityType(platformDriver: PlatformDriver) extends EntityType(Author, platformDriver) {
  val valueFields = List(
    persisted[String]("name") + viewId(classOf[R], "name", textView) + requiredString,

    viewId(classOf[R], "bookCount", intView) +
            bundleField[Int]("bookCount") +
            Getter[Int] {
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
