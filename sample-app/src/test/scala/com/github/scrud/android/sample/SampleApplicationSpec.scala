package com.github.scrud.android.sample

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.persistence.CursorField._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.github.triangle.GetterInput
import com.github.scrud.{SimpleCrudContext, CrudApplication}
import com.github.scrud.persistence._

/** A behavior specification for [[com.github.scrud.android.sample.AuthorEntityType]]
  * within [[com.github.scrud.android.sample.SampleApplication]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class SampleApplicationSpec extends FunSpec with MustMatchers with MockitoSugar {
  val application = new SampleApplication

  describe("Author") {
    it("must have the right children") {
      application.childEntityNames(Author) must be(List(Book))
    }

    it("must calculate the book count") {
      val application = mock[CrudApplication]
      val crudContext = SimpleCrudContext(application, null)
      val factory = GeneratedPersistenceFactory(new ListBufferCrudPersistence(Map.empty[String, Any], _, crudContext))
      val bookPersistence = factory.createEntityPersistence(BookEntityType, crudContext).asInstanceOf[ListBufferCrudPersistence[Map[String,Any]]]
      bookPersistence.buffer += Map.empty[String,Any] += Map.empty[String,Any]

      stub(application.persistenceFactory(BookEntityType)).toReturn(factory)
      val authorData = AuthorEntityType.copyAndUpdate(GetterInput(AuthorEntityType.toUri(100L), crudContext), Map.empty[String,Any])
      authorData must be (Map[String,Any](idFieldName -> 100L, "bookCount" -> 2))
    }
  }
}
