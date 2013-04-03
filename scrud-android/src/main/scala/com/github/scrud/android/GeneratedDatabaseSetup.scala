package com.github.scrud.android

import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import com.github.scrud.util.Common
import com.github.triangle.Logging
import android.provider.BaseColumns
import persistence.{CursorField, EntityTypePersistedInfo}
import com.github.scrud.EntityType

/**
 * An SQLiteOpenHelper for scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/12
 *         Time: 11:45 AM
 */
class GeneratedDatabaseSetup(crudContext: AndroidCrudContext, persistenceFactory: SQLitePersistenceFactory)
  extends SQLiteOpenHelper(crudContext.context, crudContext.application.nameId, null, crudContext.dataVersion) with Logging {

  protected lazy val logTag = Common.tryToEvaluate(crudContext.application.logTag).getOrElse(Common.logTag)

  private lazy val application = crudContext.application
  private lazy val entityTypesRequiringTables: Seq[EntityType] = application.allEntityTypes.filter(application.isSavable(_))

  private def createMissingTables(db: SQLiteDatabase) {
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
  }

  def onCreate(db: SQLiteDatabase) {
    createMissingTables(db)
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
    for {
      entityType <- entityTypesRequiringTables
      // skip if the entire EntityType is new since there's no need to alter the table
      if EntityTypePersistedInfo(entityType).minDataVersion <= oldVersion
      cursorField <- CursorField.persistedFields(entityType)
      if cursorField.dataVersion > oldVersion && cursorField.dataVersion <= newVersion
      command = new StringBuilder().append("ALTER TABLE ").append(SQLitePersistenceFactory.toTableName(entityType.entityName)).
          append(" ADD COLUMN ").append(cursorField.columnName).append(" ").append(cursorField.persistedType.sqliteType).append(";").toString()
    } crudContext.withExceptionReporting(execSQL(db, command))
  }
}
