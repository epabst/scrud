package com.github.scrud

import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.github.scrud.copy.{AdaptableFieldSeq, TargetType, SourceType}
import com.github.scrud.copy.types.MapStorage

/**
 * A behavior specification for [[com.github.scrud.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 10:41 PM
 */
@RunWith(classOf[JUnitRunner])
class EntityTypeSpec extends FunSpec with MustMatchers {
  describe("adapt") {
    it("must fail if no fields support the SourceType") {
      val entityType = new EntityTypeForTesting()
      val fields = AdaptableFieldSeq(entityType.Name.toAdaptableField, entityType.BirthDate.toAdaptableField)
      val unknownSourceType = new SourceType {}
      val exception = intercept[UnsupportedOperationException] {
        fields.adapt(unknownSourceType, MapStorage)
      }
      exception.getMessage must include (unknownSourceType.toString)
    }

    it("must fail if no fields support the TargetType") {
      val entityType = new EntityTypeForTesting()
      val unknownTargetType = new TargetType {}
      val exception = intercept[UnsupportedOperationException] {
        entityType.adapt(MapStorage, unknownTargetType)
      }
      exception.getMessage must include (unknownTargetType.toString)
    }
  }
}
