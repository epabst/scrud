package com.github.scala.android.crud

import android.content.ContentValues
import common.{CachedFunction, MutableListenerSet}
import persistence.{DataListener, SQLiteUtil, EntityType}

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = true

  def newWritable = new ContentValues

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new SQLiteEntityPersistence(entityType, crudContext, new GeneratedDatabaseSetup(crudContext),
      listenerSet(entityType, crudContext))

  def toTableName(entityName: String): String = SQLiteUtil.toNonReservedWord(entityName)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory

trait DataListenerSetValHolder {
  private object ListenersByEntityType
    extends LazyApplicationVal[CachedFunction[EntityType, MutableListenerSet[DataListener]]](
      CachedFunction[EntityType, MutableListenerSet[DataListener]](_ => new MutableListenerSet[DataListener]))

  def listenerSet(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[DataListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)

  def listenerHolder(entityType: EntityType, crudContext: CrudContext): MutableListenerSet[DataListener] =
    ListenersByEntityType.get(crudContext).apply(entityType)
}