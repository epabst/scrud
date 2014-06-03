package com.github.scrud.android.generate

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.android.persistence.CursorField._
import com.github.scrud.android._
import org.scalatest.mock.MockitoSugar
import com.github.scrud.{FieldName, CrudApplication, EntityType}
import com.github.scrud.android.view.ViewField
import ViewField._
import com.github.scrud.types.TitleQT
import com.github.scrud.persistence.{PersistenceFactoryForTesting, EntityTypeMapForTesting, EntityTypeMap}
import com.github.scrud.platform.representation.SummaryUI

/** A behavior specification for [[com.github.scrud.android.generate.CrudUIGenerator]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudUIGeneratorSpec extends FunSpec with MustMatchers with MockitoSugar {
  val platformDriver = new AndroidPlatformDriver(classOf[res.R])
  val displayName = "My Name"
  val viewIdFieldInfo = ViewIdFieldInfo("foo", displayName, platformDriver.field(EntityTypeForTesting.entityName, FieldName("foo"), TitleQT, Seq(SummaryUI)))

  describe("fieldLayoutForHeader") {
    it("must show the display name") {
      val position = 0
      val fieldLayout = CrudUIGenerator.fieldLayoutForHeader(viewIdFieldInfo, position)
      fieldLayout.attributes.find(_.key == "text").get.value.text must be ("My Name")
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
      val myEntityType = new EntityTypeForTesting
      val application = new CrudApplication(platformDriver, EntityTypeMapForTesting(Set[EntityType](myEntityType))) {
        val name = "Test App"
      }
      val valueStrings = CrudUIGenerator.generateValueStrings(EntityTypeViewInfo(myEntityType, application))
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").length must be (3)
    }

    it("must not include an 'add' string for unaddable entities") {
      val myEntityType = new EntityTypeForTesting {
        field("model", TitleQT, Seq(BundleStorage))
      }
      val entityTypeMap = EntityTypeMapForTesting(new PersistenceFactoryForTesting(myEntityType) {
        override def canCreate: Boolean = false
      })
      val application = new CrudApplication(platformDriver, entityTypeMap) {
        val name = "Test App"
      }
      val valueStrings = CrudUIGenerator.generateValueStrings(EntityTypeViewInfo(myEntityType, application))
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").length must be (2)
    }

    it("must not include 'add' and 'edit' strings for unmodifiable entities") {
      val _entityType = new EntityTypeForTesting {
        field("model", TitleQT, Seq(BundleStorage))
      }
      val entityTypeMap = EntityTypeMapForTesting(new PersistenceFactoryForTesting(_entityType) {
        override def canCreate: Boolean = false
        override val canSave: Boolean = false
      })
      val application = new CrudApplication(platformDriver, entityTypeMap) {
        val name = "Test App"
      }
      val valueStrings = CrudUIGenerator.generateValueStrings(EntityTypeViewInfo(_entityType, application))
      valueStrings.foreach(println(_))
      (valueStrings \\ "string").length must be (1)
    }
  }
}
