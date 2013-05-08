package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.EntityName
import com.github.scrud.android.testres.R

/** A behavior specification for [[com.github.scrud.android.generate.EntityFieldInfo]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class EntityTypeViewInfoSpec extends FunSpec with MustMatchers with MockitoSugar {
  object SelfReferencingEntity extends EntityName("SelfReferencingEntity")

  val platformDriver = new AndroidPlatformDriver(classOf[R])

  object SelfReferencingEntityType extends EntityTypeForTesting(SelfReferencingEntity, platformDriver) {
    override def valueFields = super.valueFields :+ ForeignKey(SelfReferencingEntity, namedViewField("parent", SelfReferencingEntity))
  }

  val application = new CrudApplicationForTesting(platformDriver, CrudType(EntityTypeForTesting, null), CrudType(SelfReferencingEntityType, null))

  describe("displayableViewIdFieldInfos") {
    it("must not have an infinite loop for a self-referencing EntityType") {
      val info = EntityTypeViewInfo(SelfReferencingEntityType, application)
      val fieldInfos = info.displayableViewIdFieldInfos
      fieldInfos.size must be (4)
    }
  }
}
