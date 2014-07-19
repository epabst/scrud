package com.github.scrud.platform

import com.github.scrud.copy._
import com.github.scrud.copy.AdaptableFieldWithRepresentations
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.QualifiedType

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] that simply uses MapStorage
 * for every specified SourceType and TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 3:13 PM
 */
class UniversalMapStorageAdaptableFieldFactory extends MapStorageAdaptableFieldFactory {
  override def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    val representationsByType = representations.collect {
      case representationByType: RepresentationByType[V] if !representationByType.isInstanceOf[AdaptableFieldConvertible[_]] =>
        representationByType
    }
    val applicability = representationsByType.foldLeft(FieldApplicability.Empty)(_ + toFieldApplicability(_))
    val sourceField = createSourceField[V](entityName, fieldName, qualifiedType)
    val targetField = createTargetField[V](entityName, fieldName, qualifiedType)
    val fieldByType = new AdaptableFieldByType[V](
      applicability.from.map(_ -> sourceField).toMap,
      applicability.to.map(_ -> targetField).toMap)
    AdaptableFieldWithRepresentations(fieldByType, representationsByType.toSet)
  }

  def toFieldApplicability(representation: RepresentationByType[Any]): FieldApplicability = {
    representation.toPlatformIndependentFieldApplicability
  }
}

object UniversalMapStorageAdaptableFieldFactory extends UniversalMapStorageAdaptableFieldFactory
