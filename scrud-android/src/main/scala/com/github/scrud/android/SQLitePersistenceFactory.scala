package com.github.scrud.android

import android.content.ContentValues
import persistence.SQLiteUtil
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.{CrudContext, EntityType, EntityName}
import com.github.scrud.persistence.{CrudPersistenceUsingThin, DataListenerSetValHolder, PersistenceFactory}

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends PersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  def newWritable() = new ContentValues

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    val androidCrudContext = crudContext.asInstanceOf[AndroidCrudContext]
    var initialCreate = false
    val databaseSetup = new GeneratedDatabaseSetup(androidCrudContext) {
      override def onCreate(db: SQLiteDatabase) {
        super.onCreate(db)
        initialCreate = true
      }
    }
    val thinPersistence = new SQLiteThinEntityPersistence(entityType, databaseSetup, androidCrudContext)
    val persistence = new CrudPersistenceUsingThin(entityType, thinPersistence, androidCrudContext, listenerSet(entityType, crudContext))
    if (initialCreate) {
      entityType.onCreateDatabase(persistence)
      preventRollbackOfPriorOperations(persistence)
    }
    persistence
  }


  private def preventRollbackOfPriorOperations(persistence: CrudPersistenceUsingThin) {
    // once transactions are supported, this method should end the current one and start a new one
  }

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory
