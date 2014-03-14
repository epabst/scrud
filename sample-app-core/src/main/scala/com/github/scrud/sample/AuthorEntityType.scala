package com.github.scrud.sample

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.types.{NaturalIntQT, TitleQT}
import com.github.scrud.platform.representation._
import com.github.scrud.copy.types.Validation
import com.github.scrud.copy.{Derived, Calculation}
import com.github.scrud.EntityName
import scala.Some

object Author extends EntityName("Author")

class AuthorEntityType(platformDriver: PlatformDriver) extends EntityType(Author, platformDriver) {
  val nameField = field("name", TitleQT, Seq(Persistence(1), EditUI, SelectUI, Validation.requiredString))

  val bookCount = field("bookCount", NaturalIntQT, Seq(SummaryUI,
    Calculation { requestContext => Some(requestContext.findAll(Book).size) }))

  // This is here to demo deriving a field value from another field.
  field("bookCountNeededForPopularity", NaturalIntQT, Seq(DetailUI,
    Derived(bookCount) {
      case Some(count) => Some(100 - count)
      case _ => None
    }))

  // This is here to demo deriving a field value from two other fields.
  val nickname = field("nickname", TitleQT, Seq(DetailUI, Derived(nameField, bookCount) { (nameOpt, bookCountOpt) =>
    nameOpt.map(_ + "-" + bookCountOpt.getOrElse("0").toString)
  }))
}
