package com.github.scrud.persistence

import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.context.SharedContext
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.util.{Cache, DelegatingListenerHolder}

/**
 * A pseudo-connection to any/all persistence mechanisms.
 * Actual connections to specific persistence mechanisms can be cached in the state field.
 * All actual connections that need to be closed should register themselves as listeners to the state
 * and close themselves when notified.
 * @author Eric Pabst (epabst@gmail.com)
 */
class PersistenceConnection(entityTypeMap: EntityTypeMap, val sharedContext: SharedContext)
    extends DelegatingListenerHolder[DestroyStateListener] {

  private[persistence] val state: State = new State
  protected def listenerHolder = state

  def persistenceFor(entityName: EntityName): CrudPersistence =
    persistenceFor(entityTypeMap.entityType(entityName))

  private val cache = new Cache()

  def persistenceFor(entityType: EntityType): CrudPersistence = cache.cacheBasedOn(entityType) {
    val entityPersistence = entityTypeMap.persistenceFactory(entityType).createEntityPersistence(entityType, this)
    state.addListener(new DestroyStateListener {
      def onDestroyState() {
        entityPersistence.close()
      }
    })
    entityPersistence
  }

  /** Indicate that the PersistenceConnection will no longer be used. */
  def close() {
    // This will delegate to any listeners.
    state.onDestroyState()
    cache.clear()
  }
}
