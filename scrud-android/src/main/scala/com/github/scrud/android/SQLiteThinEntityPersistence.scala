package com.github.scrud.android

import com.github.scrud.{UriPath, EntityType}
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.persistence.ThinPersistence
import com.github.triangle.{PortableField, GetterInput, Logging}
import com.github.scrud.util.Common
import collection.mutable
import android.database.Cursor
import persistence._
import android.app.backup.BackupManager
import com.github.scrud.platform.PlatformTypes._
import android.content.ContentValues
import android.provider.BaseColumns
import persistence.CursorStream
import persistence.SQLiteCriteria
import scala.Some

/**
 * A ThinPersistence for interacting with SQLite.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/12
 *         Time: 11:45 AM
 */
class SQLiteThinEntityPersistence(entityType: EntityType, database: SQLiteDatabase, crudContext: AndroidCrudContext)
    extends ThinPersistence with Logging {
  protected lazy val logTag: String = Common.tryToEvaluate(entityType.logTag).getOrElse(Common.logTag)
  private lazy val tableName = SQLitePersistenceFactory.toTableName(entityType.entityName)
  private val cursors = new mutable.SynchronizedQueue[Cursor]
  private lazy val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
  private def queryFieldNames = entityTypePersistedInfo.queryFieldNames
  private lazy val deletedEntityIdCrudType = DeletedEntityIdApplication
  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)
  private lazy val backupManager = new BackupManager(crudContext.context)

  def findAll(criteria: SQLiteCriteria): CursorStream = {
    val query = criteria.selection.mkString(" AND ")
    info("Finding each " + entityType.entityName + "'s " + queryFieldNames.mkString(", ") + " where " + query + criteria.orderBy.map(" order by " + _).getOrElse(""))
    val cursor = database.query(tableName, queryFieldNames.toArray,
      toOption(query).getOrElse(null), criteria.selectionArgs.toArray,
      criteria.groupBy.getOrElse(null), criteria.having.getOrElse(null), criteria.orderBy.getOrElse(null))
    cursors += cursor
    CursorStream(cursor, entityTypePersistedInfo)
  }

  //UseDefaults is provided here in the item list for the sake of PortableField.adjustment[SQLiteCriteria] fields
  def findAll(uri: UriPath): CursorStream =
    // The default orderBy is Some("_id desc")
    findAll(entityType.copyAndUpdate(GetterInput(uri, PortableField.UseDefaults), new SQLiteCriteria(orderBy = Some(CursorField.idFieldName + " desc"))))

  private def notifyDataChanged() {
    backupManager.dataChanged()
    debug("Notified BackupManager that data changed.")
  }

  def newWritable() = SQLitePersistenceFactory.newWritable()

  def save(idOption: Option[ID], writable: AnyRef): ID = {
    val contentValues = writable.asInstanceOf[ContentValues]
    val id = idOption match {
      case None => {
        val newId = database.insertOrThrow(tableName, null, contentValues)
        info("Added " + entityType.entityName + " #" + newId + " with " + contentValues)
        newId
      }
      case Some(givenId) => {
        info("Updating " + entityType.entityName + " #" + givenId + " with " + contentValues)
        val rowCount = database.update(tableName, contentValues, BaseColumns._ID + "=" + givenId, null)
        if (rowCount == 0) {
          contentValues.put(BaseColumns._ID, givenId)
          info("Added " + entityType.entityName + " #" + givenId + " with " + contentValues + " since id is not present yet")
          val resultingId = database.insert(tableName, null, contentValues)
          if (givenId != resultingId)
            throw new IllegalStateException("id changed from " + givenId + " to " + resultingId +
                    " when restoring " + entityType.entityName + " #" + givenId + " with " + contentValues)
        }
        givenId
      }
    }
    notifyDataChanged()
    val map = entityType.copyAndUpdate(contentValues, Map[String,Any]())
    val bytes = CrudBackupAgent.marshall(map)
    debug("Scheduled backup which will include " + entityType.entityName + "#" + id + ": size " + bytes.size + " bytes")
    id
  }

  def delete(uri: UriPath): Int = {
    val ids = findAll(uri).map { readable =>
      val id = entityType.IdField.getRequired(readable)
      database.delete(tableName, BaseColumns._ID + "=" + id, Nil.toArray)
      id
    }
    crudContext.future {
      ids.foreach { id =>
        deletedEntityIdCrudType.recordDeletion(entityType.entityName, id, crudContext)
      }
      notifyDataChanged()
    }
    ids.size
  }

  def close() {
    cursors.map(_.close())
    database.close()
  }
}
