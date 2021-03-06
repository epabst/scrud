package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.{PersistenceFactoryForTesting, EntityTypeMapForTesting}
import com.github.scrud.platform.representation.DetailUI

/** A behavior specification for [[com.github.scrud.android.generate.CrudUIGenerator]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudUIGeneratorSpec extends FunSpec with MustMatchers with MockitoSugar {
  val entityType = EntityTypeForTesting
  private val entityTypeMap = new EntityTypeMapForTesting(entityType)
  val entityTypeViewInfo = new EntityTypeViewInfo(entityType, entityTypeMap)
  val displayableEntityTypeViewInfo = new TargetedEntityTypeViewInfo(entityTypeViewInfo, DetailUI)
  val displayableViewIdFieldInfos = displayableEntityTypeViewInfo.viewIdFieldInfos
  val viewIdFieldInfo = displayableViewIdFieldInfos.head

  describe("fieldLayoutForHeader") {
    it("must show the display name") {
      val position = 0
      val fieldLayout = CrudUIGenerator.fieldLayoutForHeader(viewIdFieldInfo, position)
      fieldLayout.attributes.find(_.key == "text").get.value.text must be ("Name")
    }

    it("must put the first field on the left side of the screen") {
      val position = 0
      val fieldLayout = CrudUIGenerator.fieldLayoutForHeader(viewIdFieldInfo, position)
      fieldLayout.attributes.find(_.key == "layout_width").get.value.text must be ("wrap_content")
      fieldLayout.attributes.find(_.key == "gravity").get.value.text must be ("left")
    }

    it("must put the second field on the right side of the screen") {
      val position = 1
      val fieldLayout = CrudUIGenerator.fieldLayoutForHeader(viewIdFieldInfo, position)
      fieldLayout.attributes.find(_.key == "layout_width").get.value.text must be ("fill_parent")
      fieldLayout.attributes.find(_.key == "gravity").get.value.text must be ("right")
    }
  }

  describe("fieldLayoutForRow") {
    it("must put the first field on the left side of the screen") {
      val position = 0
      val fieldLayout = CrudUIGenerator.fieldLayoutForRow(viewIdFieldInfo, position)
      fieldLayout.head.attributes.find(_.key == "layout_width").get.value.text must be ("wrap_content")
      fieldLayout.head.attributes.find(_.key == "gravity").get.value.text must be ("left")
    }

    it("must put the second field on the right side of the screen") {
      val position = 1
      val fieldLayout = CrudUIGenerator.fieldLayoutForRow(viewIdFieldInfo, position)
      fieldLayout.head.attributes.find(_.key == "layout_width").get.value.text must be ("fill_parent")
      fieldLayout.head.attributes.find(_.key == "gravity").get.value.text must be ("right")
    }
  }

  describe("generateValueStrings") {
    it("must include 'list', 'add' and 'edit' strings for modifiable entities") {
      val valueStrings = CrudUIGenerator.generateValueStrings(entityTypeViewInfo)
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").flatMap(_.attribute("name")).map(_.text) must be (Seq("my_map_list", "add_my_map", "edit_my_map", "delete_my_map"))
    }

    it("must not include an 'add' string for unaddable entities") {
      val myEntityType = new EntityTypeForTesting {
        field("model", TitleQT, Seq(BundleStorage))
      }
      val entityTypeMap = new EntityTypeMapForTesting(myEntityType -> new PersistenceFactoryForTesting {
        override def canCreate: Boolean = false
      })
      val valueStrings = CrudUIGenerator.generateValueStrings(EntityTypeViewInfo(myEntityType, entityTypeMap))
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").flatMap(_.attribute("name")).map(_.text) must be (Seq("my_map_list", "edit_my_map", "delete_my_map"))
    }

    it("must not include 'add' and 'edit' strings for unmodifiable entities") {
      val myEntityType = new EntityTypeForTesting {
        field("model", TitleQT, Seq(BundleStorage))
      }
      val entityTypeMap = new EntityTypeMapForTesting(myEntityType -> new PersistenceFactoryForTesting {
        override def canCreate: Boolean = false
        override val canSave: Boolean = false
      })
      val valueStrings = CrudUIGenerator.generateValueStrings(EntityTypeViewInfo(myEntityType, entityTypeMap))
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").flatMap(_.attribute("name")).map(_.text) must be (Seq("my_map_list"))
    }
  }
}
