package com.github.scrud.sample.android

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.persistence.CursorField._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import com.github.triangle.{PortableField, GetterInput}
import com.github.scrud.SimpleCrudContext
import com.github.scrud.platform.TestingPlatformDriver

/** A behavior specification for [[com.github.scrud.android.sample.AuthorEntityType]]
  * within [[com.github.scrud.android.sample.SampleApplication]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class SampleApplicationSpec extends FunSpec with MustMatchers with MockitoSugar {
  val application = new SampleApplication(TestingPlatformDriver)
  import application._

  describe("Author") {
    it("must have the right children") {
      childEntityNames(Author) must be(List(Book))
    }

    it("must calculate the book count") {
      val crudContext = SimpleCrudContext(application)
      val factory = persistenceFactory(Book)
      val bookPersistence = factory.createEntityPersistence(bookEntityType, crudContext)
      bookPersistence.saveCopy(None, PortableField.UseDefaults)
      bookPersistence.saveCopy(None, PortableField.UseDefaults)
      bookPersistence.close()

      val authorData = authorEntityType.copyAndUpdate(GetterInput(Author.toUri(100L), crudContext), Map.empty[String,Option[Any]])
      authorData must be (Map[String,Option[Any]](idFieldName -> Some(100L), "bookCount" -> Some(2)))
    }
  }
}
