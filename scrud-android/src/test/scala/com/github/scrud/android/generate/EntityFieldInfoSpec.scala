package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android.testres.R
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.platform.representation.DetailUI
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.EntityTypeMapForTesting

/** A behavior specification for [[com.github.scrud.android.generate.EntityFieldInfo]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class EntityFieldInfoSpec extends FunSpec with MustMatchers with MockitoSugar {
  object EntityTypeWithBogusField extends EntityTypeForTesting {
    val bogus = field("bogus", TitleQT, Seq(DetailUI))
  }
  val entityType = EntityTypeWithBogusField
  private val entityTypeMap = EntityTypeMapForTesting(entityType)
  val application = new CrudApplicationForTesting(entityTypeMap)

  val fieldInfo_name = EntityFieldInfo(entityType.name, Seq(classOf[R.id]), entityTypeMap)

  it("must handle a viewId name that does not exist") {
    val fieldInfo = EntityFieldInfo(entityType.bogus, Seq(classOf[R.id]), entityTypeMap).displayViewIdFieldInfos.head
    fieldInfo.id must be ("bogus")
  }

  it("must consider a field displayable when it has a DisplayUI representation") {
    fieldInfo_name.isDisplayable must be (true)
  }

  it("must consider a field as non-displayable when it has no DisplayUI representation") {
    val fieldInfo = EntityFieldInfo(entityType.url, Seq(classOf[R]), entityTypeMap)
    fieldInfo.isDisplayable must be (false)
  }

  describe("displayViewIdFieldInfos") {
    it("must not include the default primary key field") {
      val fieldInfos = EntityFieldInfo(entityType.id, Seq(classOf[R]), entityTypeMap).displayViewIdFieldInfos
      fieldInfos must be(Nil)
    }
  }

  describe("editViewIdFieldInfos") {
    it("must not include the default primary key field") {
      val fieldInfos = EntityFieldInfo(entityType.id, Seq(classOf[R]), entityTypeMap).editViewIdFieldInfos
      fieldInfos must be(Nil)
    }
  }

  describe("updateableViewIdFieldInfos") {
    it("must provide a single field for an EntityView field to allow choosing Entity instance") {
      val fieldInfos = fieldInfo_name.updateableViewIdFieldInfos
      fieldInfos.map(_.id) must be (List("foo"))
      fieldInfos.map(_.layout).head.head.label must be ("Spinner")
    }
  }

  describe("displayableViewIdFieldInfos") {
    it("must provide each displayable field in the referenced EntityType for an EntityView field") {
      val fieldInfos = fieldInfo_name.displayableViewIdFieldInfos
      fieldInfos must be (EntityTypeViewInfo(EntityTypeForTesting, null).displayableViewIdFieldInfos)
    }
  }
}
