package com.github.scrud.platform.node

import com.github.scrud.copy.TypedTargetField
import com.github.scrud.context.RequestContext
import com.github.scrud.EntityName

/**
 * A TargetField for MapStorage.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/13
 *         Time: 8:58 AM
 */
class MapTargetField[V](entityName: EntityName, fieldName: String) extends TypedTargetField[MapStorage, V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def putFieldValue(target: MapStorage, valueOpt: Option[V], context: RequestContext) {
    target.put(entityName, fieldName, valueOpt)
  }
}
