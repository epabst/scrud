package com.github.scrud.copy

import com.github.scrud.context.CommandContext
import com.github.scrud.EntityName
import com.github.scrud.copy.types.MapStorage

/**
 * A TargetField for MapStorage.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/13
 *         Time: 8:58 AM
 */
case class MapTargetField[V](entityName: EntityName, fieldName: String) extends TypedTargetField[MapStorage, V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: MapStorage, valueOpt: Option[V], context: CommandContext) = {
    target.put(entityName, fieldName, valueOpt)
    target
  }
}
