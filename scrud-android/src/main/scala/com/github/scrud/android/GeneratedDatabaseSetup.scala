package com.github.scrud.android

import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import com.github.scrud.util.{DelegateLogging, ExternalLogging}
import android.provider.BaseColumns
import com.github.scrud.android.persistence.{PersistedType, SQLiteAdaptableFieldFactory}
import com.github.scrud.EntityType

/**
 * An SQLiteOpenHelper for scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/12
 *         Time: 11:45 AM
 */
class GeneratedDatabaseSetup(commandContext: AndroidCommandContext, persistenceFactory: SQLitePersistenceFactory)
  extends SQLiteOpenHelper(commandContext.context, commandContext.applicationName.toSnakeCase, null, commandContext.dataVersion) with DelegateLogging {

  override protected def loggingDelegate: ExternalLogging = commandContext.applicationName

  private lazy val entityTypeMap = commandContext.entityTypeMap
  private lazy val entityTypesRequiringTables: Seq[EntityType] = entityTypeMap.allEntityTypes.filter(entityTypeMap.isSavable(_))

  private def createMissingTables(db: SQLiteDatabase) {
    entityTypesRequiringTables.foreach { entityType =>
      val buffer = new StringBuffer
      buffer.append("CREATE TABLE IF NOT EXISTS ").append(SQLitePersistenceFactory.toTableName(entityType.entityName)).append(" (").
        append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT")
      for {
        persistedField <- entityType.currentPersistedFields
        persistedFieldName = SQLiteAdaptableFieldFactory.toPersistedFieldName(persistedField.fieldName)
        if persistedFieldName != BaseColumns._ID
        persistedType = PersistedType(persistedField.qualifiedType)
      } buffer.append(", ").append(persistedFieldName).append(" ").append(persistedType.sqliteType)
      buffer.append(")")
      execSQL(db, buffer.toString)
    }
  }

  def onCreate(db: SQLiteDatabase) {
    createMissingTables(db)
    entityTypesRequiringTables.foreach { entityType =>
      val persistence = persistenceFactory.createEntityPersistence(entityType, db, commandContext)
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
      if entityType.originalDataVersion <= oldVersion
      persistedField <- entityType.persistedFields(newVersion)
      if persistedField.persistenceRangeOpt.exists { range => range.minDataVersion > oldVersion && range.minDataVersion <= newVersion }
      persistedFieldName = SQLiteAdaptableFieldFactory.toPersistedFieldName(persistedField.fieldName)
      persistedType = PersistedType(persistedField.qualifiedType)
      command = new StringBuilder().append("ALTER TABLE ").append(SQLitePersistenceFactory.toTableName(entityType.entityName)).
          append(" ADD COLUMN ").append(persistedFieldName).append(" ").append(persistedType.sqliteType).append(";").toString()
    } commandContext.withExceptionReporting(execSQL(db, command))
  }
}
