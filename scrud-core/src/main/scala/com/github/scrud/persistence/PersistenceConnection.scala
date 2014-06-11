package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.context.{CommandContextDelegator, CommandContext}
import com.github.scrud.state.{StateHolder, DestroyStateListener, State}
import com.github.scrud.util.{Cache, DelegatingListenerHolder}

/**
 * A pseudo-connection to any/all persistence mechanisms.
 * Actual connections to specific persistence mechanisms can be cached in the state field.
 * All actual connections that need to be closed should register themselves as listeners to the state
 * and close themselves when notified.
 * @author Eric Pabst (epabst@gmail.com)
 * @param commandContext a CommandContext.  If only a SharedContext is available, use SharedContext.asStubCommandContext.
 */
class PersistenceConnection(val commandContext: CommandContext)
    extends DelegatingListenerHolder[DestroyStateListener] with StateHolder with CommandContextDelegator {

  private[persistence] val state: State = new State
  protected def listenerHolder = state

  override def applicationState: State = sharedContext.applicationState

  private val cache = new Cache()

  override def persistenceFor(entityType: EntityType): CrudPersistence = cache.cacheBasedOn(entityType) {
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
