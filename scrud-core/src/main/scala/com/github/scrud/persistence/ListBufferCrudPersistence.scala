package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.util.ListenerSet
import com.github.scrud.context.SharedContext


class ListBufferCrudPersistence[E <: AnyRef](newWritableFunction: => E, val entityType: EntityType,
                                             val sharedContext: SharedContext,
                                             listenerSet: ListenerSet[DataListener])
        extends ListBufferEntityPersistence[E](entityType.entityName, newWritableFunction, listenerSet) with TypedCrudPersistence[E]
