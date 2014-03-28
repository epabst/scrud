package com.github.scrud.sample

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.platform.{CrudOperationForTesting, PlatformDriver, TestingPlatformDriver}
import com.github.scrud.{UriPath, EntityNavigation}
import com.github.scrud.action.{OperationAction, CrudOperationType, CrudOperation}
import com.github.scrud.context._
import com.github.scrud.action.OperationAction
import com.github.scrud.context.SimpleCommandContext
import com.github.scrud.platform.CrudOperationForTesting

/**
 * Examples of using scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/18/14
 *         Time: 5:30 PM
 */
@RunWith(classOf[JUnitRunner])
class ScrudExamplesSpec extends FunSpec with MustMatchers {
  val platformDriver: PlatformDriver = TestingPlatformDriver
  val entityNavigation: EntityNavigation = new SampleEntityNavigation(platformDriver)

  describe("Normal Application Flow") {
    it("is easy to move through a normal user experience") {
      val sharedContext: SharedContext = new SimpleSharedContext(entityNavigation.entityTypeMap, platformDriver)

      val topLevelActions: Seq[OperationAction] = entityNavigation.topLevelActions
      topLevelActions must be (Seq(CrudOperationForTesting(Author, CrudOperationType.List)))

      var action = topLevelActions.head
      action.invoke(UriPath.EMPTY, new InitialCommandContext(sharedContext, entityNavigation))
      new SimpleCommandContext(action.enhead)
    }
  }
}
