package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.platform.representation.{DetailUI, SummaryUI}
import com.github.scrud.persistence.EntityTypeMapForTesting

/** A behavior specification for [[com.github.scrud.android.generate.TargetedEntityTypeViewInfo]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class TargetedEntityTypeViewInfoSpec extends FunSpec with MustMatchers with MockitoSugar {
  object SelfReferencingEntity extends EntityName("SelfReferencingEntity")

  val platformDriver = new AndroidPlatformDriver(classOf[R])

  object SelfReferencingEntityType extends EntityTypeForTesting(SelfReferencingEntity, platformDriver) {
    field("parent", SelfReferencingEntity, Seq(SummaryUI))
  }

  private val entityTypeMap = EntityTypeMapForTesting(Set[EntityType](EntityTypeForTesting, SelfReferencingEntityType))

  describe("displayableViewIdFieldInfos") {
    it("must not have an infinite loop for a self-referencing EntityType") {
      val info = EntityTypeViewInfo(SelfReferencingEntityType, entityTypeMap)
      val displayableInfo = TargetedEntityTypeViewInfo(info, DetailUI)
      val fieldInfos = displayableInfo.viewIdFieldInfos
      fieldInfos.size must be <= 10
    }
  }
}
