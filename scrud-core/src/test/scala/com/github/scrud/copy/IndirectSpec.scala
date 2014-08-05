package com.github.scrud.copy

import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.{EntityType, EntityTypeForTesting}
import com.github.scrud.platform.representation._
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.{PersistenceFactory, ListBufferPersistenceFactoryForTesting, EntityTypeMapForTesting}
import com.github.scrud.context.CommandContextForTesting
import com.github.scrud.copy.types.MapStorage
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.EntityName
import com.github.scrud.platform.representation.DisplayUI
import scala.Some

/**
 * A behavior specification for [[com.github.scrud.copy.Indirect]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/13/14
 *         Time: 11:09 PM
 */
@RunWith(classOf[JUnitRunner])
class IndirectSpec extends FunSpec with MustMatchers {
  it("must retrieve the data from the related entity") {
    val AuthorType: EntityTypeForTesting = new EntityTypeForTesting(EntityName("Author"))
    val BookType = new EntityTypeForTesting(EntityName("Book")) {
      val author = field(AuthorType.entityName, Seq(Persistence(1), EditUI))

      val authorName = field("author.name", TitleQT, Seq(DisplayUI(FieldLevel.Summary), Indirect[AuthorType.type,String](author, _.name)))
    }
    val entityTypeMap = EntityTypeMapForTesting(Map[EntityType,PersistenceFactory](AuthorType -> ListBufferPersistenceFactoryForTesting, BookType -> ListBufferPersistenceFactoryForTesting))
    val commandContext = new CommandContextForTesting(entityTypeMap)
    val authorId = commandContext.save(AuthorType.entityName, None, MapStorage, new MapStorage(AuthorType.name -> Some("Fred")))
    val bookId = commandContext.save(BookType.entityName, None, MapStorage, new MapStorage(BookType.author -> Some(authorId)))

    val authorNameOpt = commandContext.find(BookType.entityName.toUri(bookId), BookType.authorName)
    authorNameOpt must be (Some("Fred"))
  }
}
