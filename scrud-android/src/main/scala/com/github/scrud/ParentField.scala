package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle.{BaseField, DelegatingPortableField}

/** A ParentField to a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class ParentField(entityName: EntityName) extends DelegatingPortableField[ID] {
  val fieldName = entityName.name.toLowerCase + "_id"

  protected val delegate = entityName.UriPathId

  override def toString = "ParentField(" + entityName + ")"
}

object ParentField {
  def apply(entityType: EntityType): ParentField = ParentField(entityType.entityName)

  def parentFields(field: BaseField): Seq[ParentField] = field.deepCollect {
    case parentField: ParentField => parentField
  }
}
