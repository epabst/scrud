package com.github.scrud

import com.github.scrud.copy.{Representation, BaseAdaptableField}
import com.github.scrud.types.BaseQualifiedType

/**
 * A base FieldDeclaration that can be used for a sequence of heterogeneous field declarations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 12:19 AM
 */
trait BaseFieldDeclaration {
  def entityName: EntityName

  def fieldName: String

  def qualifiedType: BaseQualifiedType

  def representations: Seq[Representation[Any]]

  def toAdaptableField: BaseAdaptableField
}
