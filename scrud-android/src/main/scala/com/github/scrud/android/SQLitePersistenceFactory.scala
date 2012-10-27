package com.github.scrud.android

import android.content.ContentValues
import persistence.SQLiteUtil
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.{CrudContext, EntityType, EntityName}
import com.github.scrud.persistence.{DataListenerSetValHolder, PersistenceFactory}

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = true

  def newWritable = new ContentValues

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    val androidCrudContext = crudContext.asInstanceOf[AndroidCrudContext]
    var initialCreate = false
    val databaseSetup = new GeneratedDatabaseSetup(androidCrudContext) {
      override def onCreate(db: SQLiteDatabase) {
        super.onCreate(db)
        initialCreate = true
      }
    }
    val persistence = new SQLiteEntityPersistence(entityType, androidCrudContext, databaseSetup, listenerSet(entityType, crudContext))
    if (initialCreate) {
      entityType.onCreateDatabase(persistence)
      persistence.preventRollbackOfPriorOperations()
    }
    persistence
  }

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory
