package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{UriPath, CrudContext, EntityType}
import android.content.{ContentResolver, ContentValues}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidConversions._
import com.github.scrud.util.{DelegatingListenerSet, MutableListenerSet}
import scala.Some
import com.github.scrud.android.AndroidCrudContext
import com.github.scrud.android.view.AndroidConversions

/**
 * A PersistenceFactory that uses the ContentResolver.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 12:05 AM
 */
class ContentResolverPersistenceFactory extends PersistenceFactory with DataListenerSetValHolder { factory =>
  /** Indicates if an entity can be saved. */
  def canSave = true

  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The EntityType must support copying into this object.
    */
  def newWritable() = ContentResolverPersistenceFactory.newWritable()

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    val contentResolver = crudContext.asInstanceOf[AndroidCrudContext].context.getContentResolver
    new ContentResolverCrudPersistence(entityType, contentResolver, listenerSet(entityType, crudContext))
  }
}

object ContentResolverPersistenceFactory {
  def newWritable() = new ContentValues()
}

class ContentResolverCrudPersistence(val entityType: EntityType, contentResolver: ContentResolver,
                                     protected val listenerSet: MutableListenerSet[DataListener])
    extends CrudPersistence with DelegatingListenerSet[DataListener] {
  private lazy val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
  private lazy val queryFieldNames = entityTypePersistedInfo.queryFieldNames.toArray
  private lazy val uriPathWithEntityName = UriPath(entityType.entityName)

  def findAll(uriPath: UriPath) = {
    val uri = AndroidConversions.toUri(uriPath)
    val cursor = Option(contentResolver.query(uri, queryFieldNames, null, Array.empty, null)).getOrElse {
      sys.error("Error resolving content: " + uri)
    }
    CursorStream(cursor, entityTypePersistedInfo)
  }

  def newWritable() = ContentResolverPersistenceFactory.newWritable()

  def doSave(idOpt: Option[ID], writable: AnyRef) = idOpt match {
    case Some(id) =>
      contentResolver.update(uriPathWithEntityName / id, writable.asInstanceOf[ContentValues], null, Array.empty)
      id
    case None =>
      val newUri: UriPath = contentResolver.insert(uriPathWithEntityName, writable.asInstanceOf[ContentValues])
      newUri.findId(entityType.entityName).get
  }

  /** @return how many were deleted */
  def doDelete(uri: UriPath) = {
    contentResolver.delete(uri, null, Array.empty)
  }

  def close() {}
}
