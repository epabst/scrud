package com.github.scrud.copy

import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.QualifiedType
import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.copy.types.MapStorage

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] that enables copying to/from a MapStorage,
 * regardless of whether or not it was specified as a Representation.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
class MapStorageAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    val sourceField = createSourceField[V](entityName, fieldName, qualifiedType)
    val targetField = createTargetField[V](entityName, fieldName, qualifiedType)
    val fieldByType = new AdaptableFieldByType[V](Seq(MapStorage -> sourceField), Seq(MapStorage -> targetField))
    AdaptableFieldWithRepresentations(fieldByType, Set(MapStorage))
  }

  def createSourceField[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V]): TypedSourceField[MapStorage, V] = {
    TypedSourceField[MapStorage, V] {
      mapStorage =>
        val valueOpt = mapStorage.get(entityName, fieldName)
        valueOpt.map(_.asInstanceOf[V])
    }
  }

  def createTargetField[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V]): TypedTargetField[MapStorage, V] =
    new MapTargetField[V](entityName, fieldName)
}

object MapStorageAdaptableFieldFactory extends MapStorageAdaptableFieldFactory
