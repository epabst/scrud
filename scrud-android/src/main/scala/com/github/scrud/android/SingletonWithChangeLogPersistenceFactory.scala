package com.github.scrud.android

import common._
import persistence.{DataListener, EntityType}

/**
 * A CrudPersistence that wraps another one, but only returning the first one,
 * and creating a new instance instead of updating an existing instance.
 */
case class SingletonWithChangeLogCrudPersistence(manyPersistence: CrudPersistence,
                                                 listenerHolder: ListenerHolder[DataListener]) extends CrudPersistence
    with DelegatingListenerSet[DataListener] {

  protected def listenerSet: ListenerSet[DataListener] = manyPersistence

  def entityType = manyPersistence.entityType

  def crudContext = manyPersistence.crudContext

  private lazy val cacheClearingListener = new DataListener {
    def onChanged(uri: UriPath) { cachedFindAll.clear() }
  }

  def crudType = manyPersistence.crudContext.application.crudType(entityType)

  private lazy val cachedFindAll: CachedFunction[UriPath,Seq[AnyRef]] = {
    val function = CachedFunction((uri: UriPath) => manyPersistence.findAll(uri).take(1))
    listenerHolder.addListener(cacheClearingListener)
    function
  }

  def findAll(uri: UriPath) = cachedFindAll(uri)

  def newWritable = manyPersistence.newWritable

  // Intentionally save without reusing the ID so that existing instances are never modified since a change-log
  def doSave(id: Option[PlatformTypes.ID], writable: AnyRef) = {
    val writableWithoutId = entityType.idPkField.updateWithValue(writable, None)
    manyPersistence.doSave(None, writableWithoutId)
  }

  def doDelete(uri: UriPath) {
    manyPersistence.doDelete(uri)
  }

  def close() {
    manyPersistence.close()
    listenerHolder.removeListener(cacheClearingListener)
  }
}

/**
 * A PersistenceFactory where only the first entity instance is read, and a new instance is saved each time.
 * @author Eric Pabst (epabst@gmail.com)
 */
class SingletonWithChangeLogPersistenceFactory(delegate: PersistenceFactory) extends PersistenceFactory {
  def canSave = delegate.canSave

  override def canDelete = false

  override def canList = false

  def newWritable = delegate.newWritable

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new SingletonWithChangeLogCrudPersistence(delegate.createEntityPersistence(entityType, crudContext),
      delegate.listenerHolder(entityType, crudContext))

  /** Since the first is used, no ID is required to find one. */
  override def maySpecifyEntityInstance(entityType: EntityType, uri: UriPath) = true

  def listenerHolder(entityType: EntityType, crudContext: CrudContext) = delegate.listenerHolder(entityType, crudContext)
}
