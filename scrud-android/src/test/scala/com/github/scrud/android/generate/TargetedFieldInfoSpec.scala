package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android.testres.R
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.platform.representation.{EditUI, DetailUI}
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.EntityTypeMapForTesting

/** A behavior specification for [[com.github.scrud.android.generate.TargetedFieldInfo]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class TargetedFieldInfoSpec extends FunSpec with MustMatchers with MockitoSugar {
  object EntityTypeWithBogusField extends EntityTypeForTesting {
    val bogus = field("bogus", TitleQT, Seq(DetailUI))
  }
  val entityType = EntityTypeWithBogusField
  private val entityTypeMap = EntityTypeMapForTesting(entityType)
  val application = new CrudApplicationForTesting(entityTypeMap)

  val entityTypeViewInfo = EntityTypeViewInfo(EntityTypeForTesting, entityTypeMap)
  val fieldInfo_id = EntityFieldInfo(entityType.id, Seq(classOf[R]), entityTypeMap)
  val fieldInfo_name = EntityFieldInfo(entityType.name, Seq(classOf[R.id]), entityTypeMap)
  val fieldInfo_url = EntityFieldInfo(entityType.url, Seq(classOf[R]), entityTypeMap)
  val fieldInfo_parent = EntityFieldInfo(entityType.parent, Seq(classOf[R.id]), entityTypeMap)

  it("must handle a viewId name that does not exist") {
    val fieldInfo_bogus = EntityFieldInfo(entityType.bogus, Seq(classOf[R.id]), entityTypeMap)
    val detailFieldInfo_bogus = fieldInfo_bogus.targetedFieldInfoOrFail(DetailUI)
    detailFieldInfo_bogus.viewIdFieldInfo.id must be ("bogus")
  }

  it("must consider a field displayable when it has a DisplayUI representation") {
    val detailFieldInfo_name = fieldInfo_name.targetedFieldInfoOrFail(DetailUI)
    detailFieldInfo_name must not be null
  }

  it("must consider a field as non-displayable when it has no DisplayUI representation") {
    fieldInfo_url.findTargetedFieldInfo(DetailUI) must be (None)
  }

  describe("displayViewIdFieldInfos") {
    it("must not include the default primary key field") {
      fieldInfo_id.findTargetedFieldInfo(DetailUI) must be (None)
    }
  }

  describe("findTargetedFieldInfo") {
    it("must not include the default primary key field for the EditUI") {
      fieldInfo_id.findTargetedFieldInfo(EditUI) must be (None)
    }

    it("must provide a single field for an EntityView field to allow choosing Entity instance for the EditUI") {
      val detailFieldInfo_parent = fieldInfo_parent.targetedFieldInfoOrFail(EditUI, "edit_")
      val fieldInfos = detailFieldInfo_parent.viewIdFieldInfos
      fieldInfos.map(_.layoutForEditUI(0)).toString must include ("Spinner")
      fieldInfos.map(_.id) must be (List("edit_parent"))
    }
  }

  describe("displayableViewIdFieldInfos") {
    it("must provide each SelectUI field in the referenced EntityType for an EntityView field") {
      val displayableViewIdFieldInfos = TargetedEntityTypeViewInfo(entityTypeViewInfo, DetailUI).viewIdFieldInfos
      displayableViewIdFieldInfos.map(_.id) must be (Seq("name", "age", "parent_name"))
    }
  }
}
