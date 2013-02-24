package com.github.scrud

import android.ForeignKey._
import org.scalatest.FunSpec
import android.MyEntityType
import org.scalatest.matchers.MustMatchers
import com.github.triangle.PortableField._
import com.github.scrud.android.persistence.CursorField.persisted
import platform.TestingPlatformDriver
import scala.Some
import com.github.triangle.Getter
import com.github.triangle.&&
import java.util.concurrent.atomic.AtomicBoolean
import com.github.triangle.types.TitleQT
import view.NamedViewMap

/**
 * A specification for [[com.github.scrud.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:10 PM
 */
class EntityTypeSpec extends FunSpec with MustMatchers {
  val contextItems = new CrudContextItems(UriPath.EMPTY, new SimpleCrudContext(null))

  describe("loadingValue") {
    it("must include loading values") {
      val loadingValue = MyEntityType.loadingValue
      loadingValue.update(Map.empty[String,Option[Any]]) must be (Map("name" -> Some("...")))
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

  it("must derive parent entities from EntityField fields") {
    val entityName1 = new EntityName("Entity1")
    val entityName2 = new EntityName("Entity2")
    val entityType3 = new MyEntityType {
      override val valueFields = EntityField[EntityType](entityName1) +: EntityField[EntityType](entityName2) +: super.valueFields
    }
    entityType3.parentEntityNames must be (List(entityName1, entityName2))
  }

  it("must derive parent entities from foreignKey fields") {
    val entityName1 = new EntityName("Entity1")
    val entityName2 = new EntityName("Entity2")
    val entityType3 = new MyEntityType {
      override val valueFields = foreignKey[MyEntityType](entityName1) +: foreignKey[MyEntityType](entityName2) +: super.valueFields
    }
    entityType3.parentEntityNames must be (List(entityName1, entityName2))
  }

  it("must calculate a field when copying from the UI to persistence when the field is persisted.") {
    val entityType1 = new EntityTypeWithHiddenCalculatedField
    val uiData = NamedViewMap("string1" -> Some("Some Title"))
    val updatedPersistenceData = entityType1.copyAndUpdate(uiData +: contextItems, Map.empty[String, Option[Any]])
    updatedPersistenceData("string1") must be (Some("Some Title"))
    updatedPersistenceData("calculatedField") must be (Some("theResult"))
    entityType1.executed.get() must be (true)
  }

  it("must not calculate a field when copying from the UI to persistence when the field isn't persisted.") {
    val entityType1 = new EntityTypeWithNonPersistedCalculatedField
    val uiData = NamedViewMap("string1" -> Some("Some Title"))
    val updatedPersistenceData = entityType1.copyAndUpdate(uiData +: contextItems, Map.empty[String, Option[Any]])
    updatedPersistenceData("string1") must be (Some("Some Title"))
    updatedPersistenceData.contains("calculatedField") must be (false)
    entityType1.executed.get() must be (false)
  }

  it("must calculate a field when copying from persistence to the UI when the field is displayed.") {
    val entityType1 = new EntityTypeWithNonPersistedCalculatedField
    val persistenceData = Map[String, Option[Any]]("string1" -> Some("Some Title"))
    val updatedUiData = entityType1.copyAndUpdate(persistenceData +: contextItems, NamedViewMap())
    updatedUiData("string1") must be (Some("Some Title"))
    updatedUiData("calculatedField") must be (Some("theResult"))
    entityType1.executed.get() must be (true)
  }

  it("must not calculate a field when copying from persistence to the UI when the field isn't displayed.") {
    val entityType1 = new EntityTypeWithHiddenCalculatedField
    val persistenceData = Map[String, Option[Any]]("string1" -> Some("Some Title"))
    val updatedUiData = entityType1.copyAndUpdate(persistenceData +: contextItems, NamedViewMap())
    updatedUiData("string1") must be (Some("Some Title"))
    updatedUiData.contains("calculatedField") must be (false)
    entityType1.executed.get() must be (false)
  }
}

class EntityTypeWithNonPersistedCalculatedField extends EntityType(EntityName("Foo"), TestingPlatformDriver) {
  val executed = new AtomicBoolean(false)

  val stringField = platformDriver.namedViewField("string1", TitleQT) + persisted[String]("string1")

  val nonPersistedCalculatedField = Getter[String] {
    case stringField(Some(title)) && CrudContextField(Some(crudContext)) && UriField(Some(uri)) =>
      executed.set(true)
      Some("theResult")
  } + platformDriver.namedViewField("calculatedField", TitleQT)

  override val valueFields = stringField +: nonPersistedCalculatedField +: Nil
}

class EntityTypeWithHiddenCalculatedField extends EntityType(EntityName("Foo"), TestingPlatformDriver) {
  val executed = new AtomicBoolean(false)

  val stringField = platformDriver.namedViewField("string1", TitleQT) + persisted[String]("string1")

  val hiddenCalculatedField = Getter[String] {
    case stringField(Some(title)) && CrudContextField(Some(crudContext)) && UriField(Some(uri)) =>
      executed.set(true)
      Some("theResult")
  } + persisted("calculatedField")

  override val valueFields = stringField +: hiddenCalculatedField +: Nil
}
