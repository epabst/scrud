package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.CrudContext
import com.github.scrud.util.{MutableListenerSet, ListenerSet}


class ListBufferCrudPersistence[T <: AnyRef](newWritableFunction: => T, val entityType: EntityType,
                                             val crudContext: CrudContext,
                                             listenerSet: ListenerSet[DataListener] = new MutableListenerSet[DataListener])
        extends ListBufferEntityPersistence[T](entityType.entityName, newWritableFunction, listenerSet) with SeqCrudPersistence[T]
