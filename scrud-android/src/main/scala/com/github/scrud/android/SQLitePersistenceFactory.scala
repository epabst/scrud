package com.github.scrud.android

import android.content.ContentValues
import persistence.SQLiteUtil
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.{CrudContext, EntityType, EntityName}
import com.github.scrud.persistence.{AbstractPersistenceFactory, CrudPersistenceUsingThin, DataListenerSetValHolder}
import com.github.scrud.state.DestroyStateListener
import com.github.scrud.android.state.ActivityVar

/** A PersistenceFactory for SQLite.
  * @author Eric Pabst (epabst@gmail.com)
  */
class SQLitePersistenceFactory extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  def newWritable() = new ContentValues

  private object WritableDatabaseActivityVar extends ActivityVar[SQLiteDatabase]

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    val androidCrudContext = crudContext.asInstanceOf[AndroidCrudContext]

    val writableDatabase = WritableDatabaseActivityVar.getOrSet(crudContext.stateHolder, {
      val databaseSetup = new GeneratedDatabaseSetup(androidCrudContext, this)
      val writableDatabase = databaseSetup.getWritableDatabase
      androidCrudContext.activityState.addListener(new DestroyStateListener {
        def onDestroyState() {
          writableDatabase.close()
        }
      })
      writableDatabase
    })

    createEntityPersistence(entityType, writableDatabase, androidCrudContext)
  }

  def createEntityPersistence(entityType: EntityType, writableDatabase: SQLiteDatabase, androidCrudContext: AndroidCrudContext): CrudPersistenceUsingThin = {
    val thinPersistence = new SQLiteThinEntityPersistence(entityType, writableDatabase, androidCrudContext)
    new CrudPersistenceUsingThin(entityType, thinPersistence, androidCrudContext, listenerSet(entityType, androidCrudContext))
  }

  def toTableName(entityName: EntityName): String = SQLiteUtil.toNonReservedWord(entityName.name)
}

object SQLitePersistenceFactory extends SQLitePersistenceFactory
