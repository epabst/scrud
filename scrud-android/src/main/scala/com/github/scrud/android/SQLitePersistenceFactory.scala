package com.github.scrud.android

import android.content.ContentValues
import common.{CachedFunction, MutableListenerSet}
import entity.EntityName
import persistence.{DataListener, SQLiteUtil, EntityType}
import android.database.sqlite.SQLiteDatabase

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = true

  def newWritable = new ContentValues

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    var initialCreate = false
    val databaseSetup = new GeneratedDatabaseSetup(crudContext) {
      override def onCreate(db: SQLiteDatabase) {
        super.onCreate(db)
        initialCreate = true
      }
    }
    val persistence = new SQLiteEntityPersistence(entityType, crudContext, databaseSetup, listenerSet(entityType, crudContext))
    if (initialCreate) {
      onCreateDatabase(persistence)
    }
    persistence
  }

  /**
   * Available to be overridden as needed by applications.
   * This is especially useful to create any initial data.
   */
  protected def onCreateDatabase(persistence: SQLiteEntityPersistence) {}

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
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