package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.util.CachedFunction

/**
 * A PersistenceFactory for generated data.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:05 PM
 */
trait GeneratedPersistenceFactory[E <: AnyRef] extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = false

  def newWritable(): E = throw new UnsupportedOperationException("not supported")

  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): SeqCrudPersistence[E]
}


object GeneratedPersistenceFactory {
  /** Creates a GeneratedPersistenceFactory given a way to create a SeqCrudPersistence.  The instances are cached by EntityType. */
  def apply[E <: AnyRef](persistenceFunction: EntityType => SeqCrudPersistence[E]): GeneratedPersistenceFactory[E] = new GeneratedPersistenceFactory[E] with DataListenerSetValHolder {
    private val cachedPersistenceFunction = CachedFunction(persistenceFunction)

    def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection) = cachedPersistenceFunction(entityType)
  }
}
