package com.github.scrud.copy

import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.{EntityType, EntityName, EntityTypeForTesting}
import com.github.scrud.platform.representation.{SummaryUI, EditUI, Persistence}
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.{PersistenceFactory, ListBufferPersistenceFactoryForTesting, EntityTypeMapForTesting}
import com.github.scrud.context.RequestContextForTesting
import com.github.scrud.copy.types.MapStorage
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

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

      val authorName = field("author.name", TitleQT, Seq(SummaryUI, Indirect[AuthorType.type,String](author, _.Name)))
    }
    val entityTypeMap = EntityTypeMapForTesting(Map[EntityType,PersistenceFactory](AuthorType -> ListBufferPersistenceFactoryForTesting, BookType -> ListBufferPersistenceFactoryForTesting))
    val requestContext = new RequestContextForTesting(entityTypeMap)
    val authorId = requestContext.save(AuthorType.entityName, MapStorage, None, new MapStorage(AuthorType.Name -> Some("Fred")))
    val bookId = requestContext.save(BookType.entityName, MapStorage, None, new MapStorage(BookType.author -> Some(authorId)))

    val book = requestContext.withUri(BookType.entityName.toUri(bookId)).find(BookType.entityName, MapStorage).get
    book.get(BookType.authorName) must be (Some("Fred"))
  }
}