package com.github.scrud.android.persistence

import _root_.android.content.{ContentResolver, ContentProvider, ContentValues}
import _root_.android.database.Cursor
import _root_.android.net.Uri
import com.github.scrud._
import com.github.scrud.android.{CrudAndroidApplicationLike, AndroidCommandContext}
import android.state.ActivityStateHolder
import com.github.scrud.android.view.AndroidConversions._
import state.{ApplicationConcurrentMapVal, State}
import scala.Some
import persistence.CrudPersistence
import com.github.scrud.platform.representation.Persistence

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
  protected[scrud] def androidApplication: CrudAndroidApplicationLike
  lazy val activityState: State = new State
  lazy val commandContext = new AndroidCommandContext(getContext, this, androidApplication)
  lazy val contentResolver = getContext.getContentResolver

  def onCreate(): Boolean = true

  def getType(uri: Uri): String = {
    val uriPath = toUriPath(uri)
    uriPath.findId(uriPath.lastEntityNameOrFail) match {
      case Some(id) =>
        ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + authorityFor(androidApplication) + "." + uriPath.lastEntityNameOrFail
      case None =>
        ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + authorityFor(androidApplication) + "." + uriPath.lastEntityNameOrFail
    }
  }

  /** Allows for overriding the Uri used for notifications. */
  protected def toNotificationUri(uri: Uri): Uri = uri

  private def persistenceFor(uriPath: UriPath): CrudPersistence = {
    val entityName = UriPath.lastEntityNameOrFail(uriPath)
    CrudPersistenceByEntityName.get(this).getOrElseUpdate(entityName, commandContext.persistenceFor(entityName))
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
    val id = persistence.save(None, persistence.toWritable(ContentValuesStorage, contentValues, uriPath, commandContext))
    contentResolver.notifyChange(toNotificationUri(uri), null)
    uri.buildUpon().path(uriPath.specify(persistence.entityType.entityName, id).toString).build()
  }

  def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    //todo use selection and selectionArgs
    val uriPath = toUriPath(uri)
    val persistence = persistenceFor(uriPath)
    val writable = persistence.toWritable(ContentValuesStorage, values, uriPath, commandContext)
    commandContext.save(UriPath.lastEntityNameOrFail(uriPath), Persistence.Latest,
      persistence.entityType.idField.findFromContext(uriPath, commandContext), writable)
    val fixedUri = toUri(uriPath, commandContext.androidApplication)
    if (uri.toString != fixedUri.toString) sys.error(uri + " != " + fixedUri)
    contentResolver.notifyChange(toNotificationUri(fixedUri), null)
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
