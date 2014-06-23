package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.android.testres.R
import com.github.scrud.platform.representation.SummaryUI
import com.github.scrud.persistence.EntityTypeMapForTesting

/** A behavior specification for [[com.github.scrud.android.generate.EntityFieldInfo]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class EntityTypeViewInfoSpec extends FunSpec with MustMatchers with MockitoSugar {
  object SelfReferencingEntity extends EntityName("SelfReferencingEntity")

  val platformDriver = new AndroidPlatformDriver(classOf[R])

  object SelfReferencingEntityType extends EntityTypeForTesting(SelfReferencingEntity, platformDriver) {
    field("parent", SelfReferencingEntity, Seq(SummaryUI))
  }

  private val entityTypeMap = EntityTypeMapForTesting(Set[EntityType](EntityTypeForTesting, SelfReferencingEntityType))

  describe("displayableViewIdFieldInfos") {
    it("must not have an infinite loop for a self-referencing EntityType") {
      val info = EntityTypeViewInfo(SelfReferencingEntityType, entityTypeMap)
      val fieldInfos = info.displayableViewIdFieldInfos
      fieldInfos.size must be (4)
    }
  }
}
