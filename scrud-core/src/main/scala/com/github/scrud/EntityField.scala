package com.github.scrud

import com.github.scrud.types.QualifiedType

/**
 * A qualified field.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/17/14
 *         Time: 11:58 PM
 */
case class EntityField[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V]) {
  def this(entityName: EntityName, qualifiedType: QualifiedTypeProvidingFieldName[V]) {
    this(entityName, qualifiedType.toFieldName, qualifiedType)
  }

  def ->(valueOpt: Option[V]): (this.type, Option[V]) = (this, valueOpt)
}
