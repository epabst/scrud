package com.github.scrud.android.persistence

import _root_.android.content.{ContentResolver, ContentProvider, ContentValues}
import _root_.android.database.Cursor
import _root_.android.net.Uri
import com.github.scrud._
import android.AndroidCrudContext
import android.state.ActivityStateHolder
import com.github.scrud.android.view.AndroidConversions._
import state.{ApplicationConcurrentMapVal, State}
import scala.Some
import persistence.{PersistenceFactoryMapping, CrudPersistence}

/**
 * A ContentProvider that uses a PersistenceFactory.
 * It can be sub-classed by each application so that it can be instantiated within a foreign application.
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/18/13
 *         Time: 4:49 PM
 */
abstract class CrudContentProvider extends ContentProvider with ActivityStateHolder {
  // The reason this isn't derived from getContext.getApplicationContext is so that this ContentProvider
  // may be instantiated within a foreign application for efficiency.
  protected[scrud] def application: CrudApplication
  protected[scrud] def persistenceFactoryMapping: PersistenceFactoryMapping = application
  lazy val activityState: State = new State
  lazy val crudContext = new AndroidCrudContext(getContext, this, application)
  lazy val contentResolver = crudContext.asInstanceOf[AndroidCrudContext].context.getContentResolver

  def onCreate(): Boolean = true

  def getType(uri: Uri): String = {
    val uriPath = toUriPath(uri)
    uriPath.findId(uriPath.lastEntityNameOrFail) match {
      case Some(id) =>
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + authorityFor(persistenceFactoryMapping) + "." + uriPath.lastEntityNameOrFail
      case None =>
        ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + authorityFor(persistenceFactoryMapping) + "." + uriPath.lastEntityNameOrFail
    }
  }

  /** Allows for overriding the Uri used for notifications. */
  protected def toNotificationUri(uri: Uri): Uri = uri

  private def persistenceFor(uriPath: UriPath): CrudPersistence = {
    val entityName = uriPath.lastEntityNameOrFail
    CrudPersistenceByEntityName.get(this).getOrElseUpdate(entityName, crudContext.openEntityPersistence(entityName))
  }

  def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {
    //todo use selection and selectionArgs
    val uriPath = toUriPath(uri)
    val persistence = persistenceFor(uriPath)
    val cursor = persistence.findAll(uriPath) match {
      case cursorStream: CursorStream => cursorStream.cursor
      case results =>
        new CrudCursor(results, EntityTypePersistedInfo(persistence.entityType))
    }
    cursor.setNotificationUri(contentResolver, toNotificationUri(uri))
    cursor
  }

  def insert(uri: Uri, contentValues: ContentValues): Uri = {
    val uriPath = toUriPath(uri)
    val persistence = persistenceFor(uriPath)
    val id = persistence.save(None, persistence.toWritable(contentValues))
    contentResolver.notifyChange(toNotificationUri(uri), null)
    uri.buildUpon().path(uriPath.specify(persistence.entityType.entityName, id).toString).build()
  }

  def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    //todo use selection and selectionArgs
    val uriPath = toUriPath(uri)
    val persistence = persistenceFor(uriPath)
    val writable = persistence.toWritable(values)
    persistence.save(Some(persistence.entityType.idPkField.getRequired(uriPath)), writable)
    val uriWithoutId = toUri(uriPath.specify(uriPath.lastEntityNameOrFail), persistenceFactoryMapping)
    contentResolver.notifyChange(toNotificationUri(uriWithoutId), null)
    1
  }

  def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    //todo use selection and selectionArgs
    val uriPath = toUriPath(uri)
    val persistence = persistenceFor(uriPath)
    val result = persistence.delete(uri)
    contentResolver.notifyChange(toNotificationUri(uri), null)
    result
  }
}

private[scrud] object CrudPersistenceByEntityName extends ApplicationConcurrentMapVal[EntityName,CrudPersistence]
