package com.github.scrud

import android.ForeignKey._
import org.scalatest.FunSpec
import com.github.scrud.android.MyEntityType
import org.scalatest.matchers.MustMatchers
import com.github.triangle.PortableField._
import scala.Some

/**
 * A specification for [[com.github.scrud.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:10 PM
 */
class EntityTypeSpec extends FunSpec with MustMatchers {
  describe("loadingValue") {
    it("must include loading values") {
      val loadingValue = MyEntityType.loadingValue
      loadingValue.update(Map.empty[String,Any]) must be (Map("name" -> "..."))
    }
  }

  it("must force having an id field on subtypes") {
    val entityType = new MyEntityType {
      override val valueFields = List(mapField[String]("name"))
    }
    entityType.deepCollect {
      case f if f == entityType.UriPathId => Some(true)
    }.flatten must be (Seq(true))
  }

  it("must derive parent entities from ParentField fields") {
    val entityName1 = new EntityName("Entity1")
    val entityName2 = new EntityName("Entity2")
    val entityType3 = new MyEntityType {
      override val valueFields = ParentField(entityName1) +: ParentField(entityName2) +: super.valueFields
    }
    entityType3.parentEntityNames must be (List(entityName1, entityName2))
  }

  it("must derive parent entities from foreignKey fields") {
    val entityName1 = new EntityName("Entity1")
    val entityName2 = new EntityName("Entity2")
    val entityType1 = new MyEntityType(entityName1)
    val entityType2 = new MyEntityType(entityName2)
    val entityType3 = new MyEntityType {
      override val valueFields = foreignKey(entityType1) +: foreignKey(entityType2) +: super.valueFields
    }
    entityType3.parentEntityNames must be (List(entityName1, entityName2))
  }
}
