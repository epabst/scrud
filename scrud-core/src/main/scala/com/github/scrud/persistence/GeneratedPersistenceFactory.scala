package com.github.scrud.persistence

import com.github.scrud.EntityType

/**
 * A PersistenceFactory for generated data.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:05 PM
 */
trait GeneratedPersistenceFactory[E <: AnyRef] extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = false

  def newWritable(): E = throw new UnsupportedOperationException("not supported")

  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): TypedReadOnlyCrudPersistence[E]
}
