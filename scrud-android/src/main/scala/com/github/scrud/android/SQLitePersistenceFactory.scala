package com.github.scrud.android

import android.content.ContentValues
import com.github.scrud.android.persistence.{ContentValuesStorage, SQLiteUtil}
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.EntityType
import com.github.scrud.persistence._
import com.github.scrud.state.DestroyStateListener
import com.github.scrud.EntityName

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  def newWritable() = new ContentValues

  def writableStorageType: ContentValuesStorage.type = ContentValuesStorage

  private object WritableDatabaseVar extends PersistenceConnectionVar[SQLiteDatabase]

  override def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): CrudPersistence = {
    val androidCommandContext = persistenceConnection.commandContext.asInstanceOf[AndroidCommandContext]

    val writableDatabase = WritableDatabaseVar.getOrSet(persistenceConnection, {
      val databaseSetup = new GeneratedDatabaseSetup(androidCommandContext, this)
      val writableDatabase = databaseSetup.getWritableDatabase
      persistenceConnection.addListener(new DestroyStateListener {
        def onDestroyState() {
          writableDatabase.close()
        }
      })
      writableDatabase
    })

    createEntityPersistence(entityType, writableDatabase, androidCommandContext)
  }

  def createEntityPersistence(entityType: EntityType, writableDatabase: SQLiteDatabase, androidCommandContext: AndroidCommandContext): CrudPersistence = {
    new SQLiteCrudPersistence(entityType, writableDatabase, androidCommandContext)
  }

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory
