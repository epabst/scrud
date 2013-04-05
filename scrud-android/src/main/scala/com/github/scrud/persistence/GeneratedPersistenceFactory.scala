package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.util.CachedFunction
import com.github.scrud.CrudContext

/**
 * A PersistenceFactory for generated data.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:05 PM
 */
trait GeneratedPersistenceFactory[T <: AnyRef] extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = false

  def newWritable(): T = throw new UnsupportedOperationException("not supported")

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext): SeqCrudPersistence[T]
}


object GeneratedPersistenceFactory {
  /** Creates a GeneratedPersistenceFactory given a way to create a SeqCrudPersistence.  The instances are cached by EntityType. */
  def apply[T <: AnyRef](persistenceFunction: EntityType => SeqCrudPersistence[T]): GeneratedPersistenceFactory[T] = new GeneratedPersistenceFactory[T] with DataListenerSetValHolder {
    private val cachedPersistenceFunction = CachedFunction(persistenceFunction)

    def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = cachedPersistenceFunction(entityType)
  }
}
