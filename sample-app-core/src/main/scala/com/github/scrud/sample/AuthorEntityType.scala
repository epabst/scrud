package com.github.scrud.sample

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.TitleQT
import com.github.scrud.platform.representation.{EditUI, Persistence}

object Author extends EntityName("Author")

class AuthorEntityType(platformDriver: PlatformDriver) extends EntityType(Author, platformDriver) {
  field("name", TitleQT, Seq(Persistence, EditUI))  //todo + requiredString,
//
//    namedViewField("bookCount", NaturalIntQT) +
//            bundleField[Int]("bookCount") +
//            Getter[Int] {
//              case UriField(Some(uri)) && CrudContextField(Some(crudContext)) => {
//                println("calculating bookCount for " + uri + " and " + crudContext)
//                crudContext.withEntityPersistence(Book) { persistence =>
//                  val books = persistence.findAll(uri)
//                  Some(books.size)
//                }
//              }
//            }
//  )
}
