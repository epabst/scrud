package com.github.scrud.android.sample

import com.github.scrud.android._
import persistence.CursorField._
import com.github.scrud.{CrudContextField, EntityName, UriField, EntityType}
import com.github.triangle._
import com.github.scrud.Validation._
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{TitleQT, NaturalIntQT}

object Author extends EntityName("Author")

class AuthorEntityType(platformDriver: PlatformDriver) extends EntityType(Author, platformDriver) {
  val valueFields = List(
    persisted[String]("name") + namedViewField("name", TitleQT) + requiredString,

    namedViewField("bookCount", NaturalIntQT) +
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
