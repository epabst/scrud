package com.github.scrud.android

import com.github.scrud.{UriPath, EntityType}
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.persistence.{DataListener, CrudPersistence}
import collection.mutable
import android.database.Cursor
import persistence._
import android.app.backup.BackupManager
import com.github.scrud.platform.PlatformTypes._
import android.content.ContentValues
import android.provider.BaseColumns
import persistence.CursorStream
import scala.Some
import com.github.scrud.android.backup.CrudBackupAgent
import com.github.scrud.android.persistence.SQLiteCriteria
import com.github.scrud.platform.representation.Query
import com.github.scrud.copy.{StorageType, SourceType}
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.util._
import com.github.scrud.context.SharedContext
import com.github.scrud.android.persistence.EntityTypePersistedInfo

/**
 * A CrudPersistence for interacting with SQLite.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/17/12
 *         Time: 11:45 AM
 */
class SQLiteCrudPersistence(val entityType: EntityType, database: SQLiteDatabase,
                            protected val commandContext: AndroidCommandContext,
                            protected val listenerSet: ListenerSet[DataListener])
    extends CrudPersistence with DelegatingListenerSet[DataListener] with DelegateLogging {

  private lazy val tableName = SQLitePersistenceFactory.toTableName(entityType.entityName)
  private val cursors = new mutable.SynchronizedQueue[Cursor]
  private lazy val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
  private def queryFieldNames = entityTypePersistedInfo.queryFieldNames
  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)
  private lazy val backupManager = new BackupManager(commandContext.context)

  override def sharedContext: SharedContext = commandContext.sharedContext

  override protected def loggingDelegate: ExternalLogging = commandContext.applicationName

  /** This override is needed because findAll returns a CursorStream which returns a copy of a Cursor row. */
  override def sourceType: SourceType = CursorStream.storageType

  def findAll(criteria: SQLiteCriteria): CursorStream = {
    val query = criteria.selection.mkString(" AND ")
    info("Finding each " + entityType.entityName + "'s " + queryFieldNames.mkString(", ") + " where " + query + criteria.orderBy.fold("")(" order by " + _))
    val cursor = database.query(tableName, queryFieldNames.toArray,
      toOption(query).getOrElse(null), criteria.selectionArgs.toArray,
      criteria.groupBy.getOrElse(null), criteria.having.getOrElse(null), criteria.orderBy.getOrElse(null))
    cursors += cursor
    CursorStream(cursor, entityTypePersistedInfo, commandContext)
  }

  //UseDefaults is provided here in the item list for the sake of PortableField.adjustment[SQLiteCriteria] fields
  def findAll(uri: UriPath): CursorStream = {
    // The default orderBy is Some("_id desc")
    val criteria = entityType.copyAndUpdate(SourceType.none, SourceType.none, uri, Query, new SQLiteCriteria(orderBy = Some(entityType.idFieldName + " desc")), commandContext)
    findAll(criteria)
  }

  private def notifyDataChanged() {
    backupManager.dataChanged()
    debug("Notified BackupManager that data changed.")
  }

  def newWritable() = SQLitePersistenceFactory.newWritable()

  /** The type that is returned by newWritable(). */
  override def writableType: StorageType = SQLitePersistenceFactory.writableStorageType

  override protected def doSave(idOption: Option[ID], writable: AnyRef): ID = {
    val contentValues = writable.asInstanceOf[ContentValues]
    val id = idOption match {
      case None =>
        val newId = database.insertOrThrow(tableName, null, contentValues)
        info("Added " + entityType.entityName + " #" + newId + " with " + contentValues)
        newId
      case Some(givenId) =>
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
    notifyDataChanged()
    val mapStorage = entityType.copyAndUpdate(ContentValuesStorage, contentValues, entityType.toUri(id), MapStorage, commandContext)
    val bytes = CrudBackupAgent.marshall(mapStorage.toMap)
    debug("Scheduled backup which will include " + entityType.entityName + "#" + id + ": size " + bytes.size + " bytes")
    id
  }

  /** @return how many were deleted */
  override def doDelete(uri: UriPath): Int = {
    val ids = findAll(uri, entityType.id, commandContext).map { id =>
      database.delete(tableName, BaseColumns._ID + "=" + id, Nil.toArray)
      id
    }
    commandContext.future {
      ids.foreach { id =>
        commandContext.androidApplication.deletedEntityTypeMap.recordDeletion(entityType.entityName, id, commandContext)
      }
      notifyDataChanged()
    }
    ids.size
  }

  def close() {
    cursors.map(_.close())
  }
}
