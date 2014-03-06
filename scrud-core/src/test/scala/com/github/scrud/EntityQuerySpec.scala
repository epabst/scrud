package com.github.scrud

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.platform.representation.Query
import com.github.scrud.query.{FieldPathValue, EntityQuery}

/**
 * A behavior specification for [[com.github.scrud.query.EntityQuery]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/27/14
 *         Time: 3:46 PM
 */
@RunWith(classOf[JUnitRunner])
class EntityQuerySpec extends FunSpec with MustMatchers {
  object A extends EntityName("A")

  object EntityTypeA extends EntityType(A, TestingPlatformDriver) {
    val b = field(B, Seq(Query))
  }

  object B extends EntityName("B")

  object EntityTypeB extends EntityType(B, TestingPlatformDriver)

  describe("...") {
    it("must switch to another Entity using an ID in a foreign key field") {
      val queryA = EntityQuery[EntityTypeA.type](A)
      val queryB = queryA.selectID(34).traverse(_.b)
      queryB must be (EntityQuery[EntityTypeB.type](B, Seq(FieldPathValue(EntityFieldPath(), ))))
    }
  }
}
