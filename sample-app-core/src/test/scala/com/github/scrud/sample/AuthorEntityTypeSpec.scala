package com.github.scrud.sample

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.platform.TestingPlatformDriver
import org.scalatest.matchers.MustMatchers
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.context.RequestContextForTesting

/**
 * A behavior specification for [[com.github.scrud.sample.AuthorEntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/24/14
 *         Time: 4:02 PM
 */
@RunWith(classOf[JUnitRunner])
class AuthorEntityTypeSpec extends FunSpec with MustMatchers {
  val entityTypeMap = new SampleEntityTypeMap(TestingPlatformDriver)
  val authorType = entityTypeMap.authorEntityType
  val bookType = entityTypeMap.bookEntityType

  it("must calculate the book count") {
    val requestContext = new RequestContextForTesting(entityTypeMap)
    val authorId = requestContext.save(Author, MapStorage, None, new MapStorage(authorType.nameField -> Some("Tolkien")))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Hobbit"), bookType.author -> Some(authorId)))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Fellowship of the Ring"), bookType.author -> Some(authorId)))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Two Towers"), bookType.author -> Some(authorId)))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Return of the King"), bookType.author -> Some(authorId)))
    val author = requestContext.findAll(Author, MapStorage).head
    author.get(authorType.bookCount) must be (Some(4))
  }

  it("must calculate the author's nick name using the name and bookCount") {
    val requestContext = new RequestContextForTesting(entityTypeMap)
    val authorId = requestContext.save(Author, MapStorage, None, new MapStorage(
      authorType.nameField -> Some("Tolkien")))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Hobbit"), bookType.author -> Some(authorId)))
    requestContext.save(Book, MapStorage, None, new MapStorage(
      bookType.nameField -> Some("The Fellowship of the Ring"), bookType.author -> Some(authorId)))
    val author = requestContext.findAll(Author, MapStorage).head
    author.get(authorType.nickname) must be (Some("Tolkien-2"))
  }
}
