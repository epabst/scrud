package com.github.scala.android.crud

import android.content.ContentValues
import common.{CachedFunction, MutableListenerSet}
import persistence.{PersistenceListener, SQLiteUtil, EntityType}

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
object SQLitePersistenceFactory extends PersistenceFactory with PersistenceListenerSetValHolder {
  def canSave = true

  def newWritable = new ContentValues

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new SQLiteEntityPersistence(entityType, crudContext, listenerSet(entityType, crudContext))


  def toTableName(entityName: String): String = SQLiteUtil.toNonReservedWord(entityName)
}

trait PersistenceListenerSetValHolder {
  private object ListenersByEntityType
    extends LazyApplicationVal[CachedFunction[EntityType, MutableListenerSet[PersistenceListener]]](
      CachedFunction[EntityType, MutableListenerSet[PersistenceListener]](_ => new MutableListenerSet[PersistenceListener]))

  def listenerSet(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[PersistenceListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)

  def listenerHolder(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[PersistenceListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)
}