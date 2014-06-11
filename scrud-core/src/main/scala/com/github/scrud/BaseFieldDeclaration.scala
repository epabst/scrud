package com.github.scrud

import com.github.scrud.copy.{Representation, BaseAdaptableField}
import com.github.scrud.types.BaseQualifiedType
import com.github.scrud.platform.representation.PersistenceRange

/**
 * A base FieldDeclaration that can be used for a sequence of heterogeneous field declarations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 12:19 AM
 */
abstract class BaseFieldDeclaration {
  def entityName: EntityName

  def fieldName: FieldName

  def qualifiedType: BaseQualifiedType

  def representations: Seq[Representation[Any]]

  def toAdaptableField: BaseAdaptableField

  lazy val persistenceRangeOpt: Option[PersistenceRange] =
    (for {
      persistenceRange <- representations.collect {
        case persistenceRange: PersistenceRange => persistenceRange
      }
    } yield persistenceRange).headOption
}
