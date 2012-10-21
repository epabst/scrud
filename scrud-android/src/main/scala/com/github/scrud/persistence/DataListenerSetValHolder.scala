package com.github.scrud.persistence

import com.github.scrud.util.{CachedFunction, MutableListenerSet}
import com.github.scrud.EntityType
import com.github.scrud.state.LazyApplicationVal
import com.github.scrud.android.CrudContext

/**
 * A holder of DataListeners.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:51 PM
 */
trait DataListenerSetValHolder {
  private object ListenersByEntityType
    extends LazyApplicationVal[CachedFunction[EntityType, MutableListenerSet[DataListener]]](
      CachedFunction[EntityType, MutableListenerSet[DataListener]](_ => new MutableListenerSet[DataListener]))

  def listenerSet(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[DataListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)

  def listenerHolder(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[DataListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)
}
