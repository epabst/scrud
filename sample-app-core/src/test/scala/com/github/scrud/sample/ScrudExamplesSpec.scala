package com.github.scrud.sample

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.context._
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.copy.SourceType
import com.github.scrud.UriPath

/**
 * Examples of using scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/18/14
 *         Time: 5:30 PM
 */
@RunWith(classOf[JUnitRunner])
class ScrudExamplesSpec extends FunSpec with MustMatchers {
  val platformDriver: PlatformDriver = TestingPlatformDriver
  val entityNavigation = new SampleEntityNavigation(platformDriver)
  val entityTypeMap = entityNavigation.entityTypeMap
  val authorEntityType = entityTypeMap.authorEntityType

  val UIStorageType = MapStorage
  type UIStorageType = MapStorage

  describe("Normal Application Flow") {
    it("is easy to move through a normal user experience") {
      val sharedContext: SharedContext = new SimpleSharedContext(entityNavigation.entityTypeMap)
      // Normally this CommandContext would come from the platform.
      val commandContext: CommandContext = sharedContext.asStubCommandContext

      // Get the list of authors to show.
      val authors = commandContext.findAll(UriPath(Author), UIStorageType)
      println("Here is the list of authors: " + authors)

      // The user clicks on an "Add" button and is asked for the author data, populated with defaults.
      val defaultAuthorData = authorEntityType.copyAndUpdate(SourceType.none, SourceType.none, UriPath(Author), UIStorageType, commandContext)
      println("Please enter the author's information.  Here are the defaults: " + defaultAuthorData)

      // The user provides some data and clicks "Save".
      val userModifiedAuthorDataToAdd = new UIStorageType(authorEntityType.nameField -> Some("George"))

      // The user clicks "Save".
      val newAuthorId = commandContext.save(Author, UIStorageType, None, userModifiedAuthorDataToAdd)

      // Refresh the list of authors showing.
      val updatedAuthors = commandContext.findAll(UriPath(Author), UIStorageType)
      println("Here is the updated list of authors: " + updatedAuthors)
                                                
      // The user clicks "Edit" on the author.
      val authorDataToEdit = commandContext.find(Author.toUri(newAuthorId), UIStorageType).get
      println("Please update the author's information.  Here is the current data: " + authorDataToEdit)

      // The user modifies the data
      val userModifiedAuthorDataToSave = authorDataToEdit
      
      // The user clicks "Save".
      commandContext.save(Author, UIStorageType, Some(newAuthorId), userModifiedAuthorDataToSave)

      // The user clicks "Delete" for the author.
      commandContext.delete(Author.toUri(newAuthorId))
    }
  }
}
