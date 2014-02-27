package com.github.scrud.platform

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.EntityName

/**
 * The behavior specification for [[com.github.scrud.platform.TestingPlatformDriver]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 11:16 AM
 */
@RunWith(classOf[JUnitRunner])
class TestingPlatformDriverSpec extends PlatformDriverContract {
  protected def makePlatformDriver() = TestingPlatformDriver

  describe("idFieldName") {
    it("must be 'id' for a primary key") {
      val fieldName = makePlatformDriver().idFieldName(EntityName("MyEntity"))
      fieldName must be ("id")
    }

    it("must make the first character lower-case and append 'Id' for an external reference") {
      val fieldName = makePlatformDriver().idFieldName(EntityName("MyEntity"))
      fieldName must be ("myEntityId")
    }
  }
}
