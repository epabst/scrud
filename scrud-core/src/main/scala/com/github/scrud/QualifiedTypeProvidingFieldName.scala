package com.github.scrud

import com.github.scrud.types.QualifiedType

/**
 * A QualifiedType that provides a default field name.
 * @see EntityType.field
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/26/14
 *         Time: 10:51 PM
 */
trait QualifiedTypeProvidingFieldName[T] extends QualifiedType[T] {
  def toFieldName: String
}
