package com.github.scala.android.crud

import common._
import persistence.{PersistenceListener, EntityType}

/**A CrudPersistence that wraps another one, but only returning the first one,
 * and creating a new instance instead of updating an existing instance.
 */
case class SingletonWithChangeLogCrudPersistence(manyPersistence: CrudPersistence) extends CrudPersistence
    with DelegatingListenerSet[PersistenceListener] {

  protected def listenerSet = manyPersistence

  def entityType = manyPersistence.entityType

  def crudContext = manyPersistence.crudContext

  def findAll(uri: UriPath) = manyPersistence.findAll(uri).take(1)

  def newWritable = manyPersistence.newWritable

  // Intentionally save without reusing the ID so that existing instances are never modified since a change-log
  def doSave(id: Option[PlatformTypes.ID], writable: AnyRef) = {
    val writableWithoutId = entityType.idPkField.transformer.apply(writable)(None)
    manyPersistence.doSave(None, writableWithoutId)
  }

  def doDelete(uri: UriPath) {
    manyPersistence.doDelete(uri)
  }

  def close() {
    manyPersistence.close()
  }
}

/**A PersistenceFactory where only the first entity instance is read, and a new instance is saved each time.
 * @author Eric Pabst (epabst@gmail.com)
 */
class SingletonWithChangeLogPersistenceFactory(delegate: PersistenceFactory) extends PersistenceFactory {
  def canSave = delegate.canSave

  override def canDelete = false

  override def canList = false

  def newWritable = delegate.newWritable

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new SingletonWithChangeLogCrudPersistence(delegate.createEntityPersistence(entityType, crudContext))

  /**Since the first is used, no ID is required to find one. */
  override def maySpecifyEntityInstance(entityType: EntityType, uri: UriPath) = true

  def addListener(listener: PersistenceListener, entityType: EntityType, crudContext: CrudContext) {
    delegate.addListener(listener, entityType, crudContext)
  }
}
