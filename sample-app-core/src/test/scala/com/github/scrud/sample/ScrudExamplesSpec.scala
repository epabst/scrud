package com.github.scrud.sample

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.action._
import com.github.scrud.context._
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.copy.SourceType

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

  describe("Normal Application Flow") {
    it("is easy to move through a normal user experience") {
      val sharedContext: SharedContext = new SimpleSharedContext(entityNavigation.entityTypeMap, platformDriver)
      val commandContext: CommandContext = sharedContext.asStubCommandContext

      val initialViewRequest = entityNavigation.initialViewRequest(commandContext)
      platformDriver.render(initialViewRequest, commandContext)

      initialViewRequest.entityNameOpt must be (Some(Author))
      initialViewRequest.entityIdOpt must be (None)
      initialViewRequest.availableCommands.map(_.actionKeyAndEntityNameOrFail) must be (Seq(ActionKey.Add -> Author))
      initialViewRequest.availableCommands.map(_.entityIdOpt) must be (Seq(None))
      initialViewRequest.availableCommands.map(_.actionDataTypeOpt) must be (Seq(None))

      val addAuthorCommand = initialViewRequest.availableCommands.head
      val createAuthorViewRequest = entityNavigation.invoke(addAuthorCommand, None, commandContext)
      createAuthorViewRequest.entityNameOpt must be (Some(Author))
      createAuthorViewRequest.entityIdOpt must be (None)
      createAuthorViewRequest.availableCommands.map(_.actionKey) must be (Seq(ActionKey.Save))
      val createAuthorCommand = initialViewRequest.availableCommands.head
      val defaultAuthorData = authorEntityType.copyAndUpdate(SourceType.none, SourceType.none, MapStorage, commandContext)

      // Simulate a user providing some data
      val userModifiedActionData = authorEntityType.copyAndUpdate(MapStorage, new MapStorage(
        authorEntityType.nameField -> Some("George")), MapStorage, defaultAuthorData, commandContext)

      val newAuthorViewRequest = entityNavigation.invoke(createAuthorCommand, Some(userModifiedActionData), commandContext)
      newAuthorViewRequest.entityNameOpt must be (Some(Author))
      val Some(newAuthorId) = newAuthorViewRequest.entityIdOpt
      newAuthorViewRequest.availableCommands.map(_.actionKeyAndEntityNameOrFail) must be (Seq(ActionKey.Edit -> Author))

      sharedContext.withPersistence(_.find(Author, newAuthorId, authorEntityType.nameField, commandContext)) must be (Some("George"))
    }
  }
}
