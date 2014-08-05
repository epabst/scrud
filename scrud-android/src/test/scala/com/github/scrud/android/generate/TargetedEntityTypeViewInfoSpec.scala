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

  val platformDriver = AndroidPlatformDriverForTesting

  object SelfReferencingEntityType extends EntityTypeForTesting(SelfReferencingEntity, platformDriver) {
    field("parent", SelfReferencingEntity, Seq(SummaryUI))
  }

  private val entityTypeMap = EntityTypeMapForTesting(Set[EntityType](EntityTypeForTesting, SelfReferencingEntityType))

  describe("viewIdFieldInfos") {
    it("must not have an infinite loop for a self-referencing EntityType") {
      val info = EntityTypeViewInfo(SelfReferencingEntityType, entityTypeMap)
      val displayableInfo = TargetedEntityTypeViewInfo(info, DetailUI)
      val fieldInfos = displayableInfo.viewIdFieldInfos
      fieldInfos.size must be <= 10
    }

    it("must use implied target types for DisplayUI representations") {
      val entityType = new EntityTypeForTesting(platformDriver = CrudUIGeneratorForTesting.platformDriver)
      val info = EntityTypeViewInfo(entityType, entityTypeMap)
      val displayableInfo = TargetedEntityTypeViewInfo(info, SummaryUI)
      displayableInfo.viewIdFieldInfos.map(_.field) must be (Seq(entityType.name, entityType.age))
    }
  }
}
