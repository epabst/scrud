package com.github.scrud.android

import common.PlatformTypes._
import entity.EntityName
import persistence.CursorField._
import android.provider.BaseColumns
import com.github.triangle.{PortableField, BaseField, DelegatingPortableField}
import persistence.EntityType

/** A ParentField to a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class ParentField(entityName: EntityName) extends DelegatingPortableField[ID] {
  val fieldName = entityName.name.toLowerCase + BaseColumns._ID

  protected val delegate = entityName.UriPathId

  override def toString = "ParentField(" + entityName + ")"
}

object ParentField {
  def apply(entityType: EntityType): ParentField = ParentField(entityType.entityName)

  def parentFields(field: BaseField): Seq[ParentField] = field.deepCollect {
    case parentField: ParentField => parentField
  }

  def foreignKey(entityName: EntityName): PortableField[ID] = {
    val parentField = ParentField(entityName)
    parentField + persisted[ID](parentField.fieldName) + sqliteCriteria[ID](parentField.fieldName)
  }

  def foreignKey(entityType: EntityType): PortableField[ID] = foreignKey(entityType.entityName)
}
