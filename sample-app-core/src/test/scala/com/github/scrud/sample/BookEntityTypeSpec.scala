package com.github.scrud.sample

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.platform.representation.{MapStorage, Persistence}
import org.scalatest.matchers.MustMatchers

/**
 * A behavior specification for [[com.github.scrud.sample.BookEntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/6/14
 *         Time: 3:17 PM
 */
@RunWith(classOf[JUnitRunner])
class BookEntityTypeSpec extends FunSpec with MustMatchers {
  val entityType = new BookEntityType(TestingPlatformDriver)

  it("must have a default genre of Fantasy") {
    val sourceField = entityType.genre.findSourceField(Persistence)
    sourceField.flatMap(_.findValue(new MapStorage, null)) must be (Some(Genre.Fantasy))
  }
}
