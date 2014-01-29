package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.util.{MutableListenerSet, ListenerSet}
import com.github.scrud.context.SharedContext


class ListBufferCrudPersistence[T <: AnyRef](newWritableFunction: => T, val entityType: EntityType,
                                             val sharedContext: SharedContext,
                                             listenerSet: ListenerSet[DataListener] = new MutableListenerSet[DataListener])
        extends ListBufferEntityPersistence[T](entityType.entityName, newWritableFunction, listenerSet) with SeqCrudPersistence[T]
