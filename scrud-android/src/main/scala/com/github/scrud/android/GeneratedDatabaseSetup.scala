package com.github.scrud.android

import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import com.github.scrud.util.Common
import com.github.triangle.Logging
import android.provider.BaseColumns
import persistence.EntityTypePersistedInfo
import com.github.scrud.EntityType

/**
 * An SQLiteOpenHelper for scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/12
 *         Time: 11:45 AM
 */
class GeneratedDatabaseSetup(crudContext: AndroidCrudContext, persistenceFactory: SQLitePersistenceFactory)
  extends SQLiteOpenHelper(crudContext.activityContext, crudContext.application.nameId, null, crudContext.application.dataVersion) with Logging {

  protected lazy val logTag = Common.tryToEvaluate(crudContext.application.logTag).getOrElse(Common.logTag)

  private def createMissingTables(db: SQLiteDatabase): Seq[EntityType] = {
    val application = crudContext.application
    val entityTypesRequiringTables = application.allEntityTypes.filter(application.isSavable(_))
    entityTypesRequiringTables.foreach { entityType =>
      val buffer = new StringBuffer
      buffer.append("CREATE TABLE IF NOT EXISTS ").append(SQLitePersistenceFactory.toTableName(entityType.entityName)).append(" (").
        append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT")
      EntityTypePersistedInfo(entityType).persistedFields.filter(_.columnName != BaseColumns._ID).foreach { persisted =>
        buffer.append(", ").append(persisted.columnName).append(" ").append(persisted.persistedType.sqliteType)
      }
      buffer.append(")")
      execSQL(db, buffer.toString)
    }
    entityTypesRequiringTables
  }

  def onCreate(db: SQLiteDatabase) {
    val entityTypesRequiringTables = createMissingTables(db)
    entityTypesRequiringTables.foreach { entityType =>
      val persistence = persistenceFactory.createEntityPersistence(entityType, db, crudContext)
      entityType.onCreateDatabase(persistence)
      // don't close persistence here since it will be closed by whatever triggered this onCreate.
    }
  }

  private def execSQL(db: SQLiteDatabase, sql: String) {
    debug("execSQL: " + sql)
    db.execSQL(sql)
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // Steps to upgrade the database for the new version ...
    createMissingTables(db)
  }
}
