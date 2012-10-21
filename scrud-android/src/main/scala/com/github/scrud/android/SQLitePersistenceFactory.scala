package com.github.scrud.android

import android.content.ContentValues
import persistence.SQLiteUtil
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.persistence.{DataListenerSetValHolder, PersistenceFactory}

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
