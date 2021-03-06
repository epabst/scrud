package com.github.scrud.copy

import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.copy.types.MapStorage

/**
 * A TargetField for MapStorage.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/13
 *         Time: 8:58 AM
 */
case class MapTargetField[V](entityName: EntityName, fieldName: FieldName) extends TypedTargetField[MapStorage, V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: MapStorage, valueOpt: Option[V], context: CopyContext) = {
    target.put(entityName, fieldName, valueOpt)
    target
  }
}
