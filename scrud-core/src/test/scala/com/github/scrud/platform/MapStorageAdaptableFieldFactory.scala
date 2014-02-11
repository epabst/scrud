package com.github.scrud.platform

import com.github.scrud.copy._
import com.github.scrud.platform.representation.{MapStorage, RepresentationByType}
import com.github.scrud.copy.AdaptableFieldAndUnusedRepresentations
import com.github.scrud.copy.MapTargetField
import com.github.scrud.EntityName
import com.github.scrud.types.QualifiedType

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] that simply uses MapStorage
 * for every specified SourceType and TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 3:13 PM
 */
class MapStorageAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], baselineFieldAndUnused: AdaptableFieldAndUnusedRepresentations[V]) = {
    val representationsByType = baselineFieldAndUnused.representationsWithType[RepresentationByType]
    val applicability = representationsByType.foldLeft(FieldApplicability.Empty)(_ + toFieldApplicability(_))
    val sourceField = TypedSourceField[MapStorage,V] { mapStorage =>
      val valueOpt = mapStorage.get(entityName, fieldName)
      valueOpt.map(_.asInstanceOf[V])
    }
    val targetField = new MapTargetField[V](entityName, fieldName)
    val fieldByType = new AdaptableFieldByType[V](
      applicability.from.map(_ -> sourceField).toMap,
      applicability.to.map(_ -> targetField).toMap)
    baselineFieldAndUnused.orElse(fieldByType, representationsByType)
  }

  def toFieldApplicability(representation: RepresentationByType): FieldApplicability = {
    representation.toPlatformIndependentFieldApplicability
  }
}

object MapStorageAdaptableFieldFactory extends MapStorageAdaptableFieldFactory
