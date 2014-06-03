package com.github.scrud.android

import android.content.ContentValues
import persistence.SQLiteUtil
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.persistence._
import com.github.scrud.state.DestroyStateListener
import com.github.scrud.android.state.ActivityVar
import com.github.scrud.context.CommandContext
import com.github.scrud.EntityName

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  def newWritable() = new ContentValues

  private object WritableDatabaseVar extends PersistenceConnectionVar[SQLiteDatabase]

  override def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): CrudPersistence = {
    val androidCommandContext = persistenceConnection

    val writableDatabase = WritableDatabaseVar.getOrSet(persistenceConnection, {
      val databaseSetup = new GeneratedDatabaseSetup(persistenceConnection, this)
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

  def createEntityPersistence(entityType: EntityType, writableDatabase: SQLiteDatabase, androidCommandContext: AndroidCommandContext): CrudPersistenceUsingThin = {
    val thinPersistence = new SQLiteThinEntityPersistence(entityType, writableDatabase, androidCommandContext)
    new CrudPersistenceUsingThin(entityType, thinPersistence, androidCommandContext.sharedContext)
  }

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory
