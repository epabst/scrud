package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{UriPath, EntityType}
import android.content.{ContentResolver, ContentValues}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidConversions._
import com.github.scrud.util.{ListenerHolder, DelegatingListenerSet}
import scala.Some
import com.github.scrud.android.view.AndroidConversions
import android.net.Uri
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.android.action.AndroidCommandContextDelegator
import com.github.scrud.copy.{StorageType, SourceType}

/**
 * A [[com.github.scrud.persistence.CrudPersistence]] that delegates to ContentResolver.
 * ic Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 3:57 PM
 */
class ContentResolverCrudPersistence(val entityType: EntityType, contentResolver: ContentResolver,
                                     entityTypeMap: EntityTypeMap, protected val commandContext: AndroidCommandContext,
                                     protected val listenerSet: ListenerHolder[DataListener])
    extends CrudPersistence with DelegatingListenerSet[DataListener] with AndroidCommandContextDelegator {
  private lazy val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
  private lazy val queryFieldNames = entityTypePersistedInfo.queryFieldNames.toArray
  private lazy val uriPathWithEntityName = UriPath(entityType.entityName)
  private lazy val applicationUri = AndroidConversions.baseUriFor(commandContext.androidApplication)

  private def toUri(uriPath: UriPath): Uri = {
    AndroidConversions.withAppendedPath(applicationUri, uriPath)
  }

  /** This override is needed because findAll returns a CursorStream which returns a copy of a Cursor row. */
  override def sourceType: SourceType = CursorStream.storageType

  def findAll(uriPath: UriPath) = {
    val uri = toUri(uriPath.specifyLastEntityName(entityType.entityName))
    val cursor = Option(contentResolver.query(uri, queryFieldNames, null, Array.empty, null)).getOrElse {
      sys.error("Error resolving content: " + uri)
    }
    CursorStream(cursor, entityTypePersistedInfo, sharedContext.asStubCommandContext)
  }

  def newWritable() = ContentResolverPersistenceFactory.newWritable()

  override def writableType: StorageType = ContentResolverPersistenceFactory.writableStorageType

  def doSave(idOpt: Option[ID], writable: AnyRef) = idOpt match {
    case Some(id) =>
      contentResolver.update(toUri(uriPathWithEntityName / id), writable.asInstanceOf[ContentValues], null, Array.empty)
      id
    case None =>
      val newUri: UriPath = contentResolver.insert(toUri(uriPathWithEntityName), writable.asInstanceOf[ContentValues])
      newUri.findId(entityType.entityName).get
  }

  /** @return how many were deleted */
  def doDelete(uri: UriPath) = {
    contentResolver.delete(toUri(uri), null, Array.empty)
  }

  def close() {}
}
