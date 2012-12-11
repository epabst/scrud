package com.github.scrud.android.view

import com.github.scrud.persistence.{DataListener, CrudPersistence}
import com.github.scrud.{UriPath, CrudContextItems}
import android.app.Activity
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.persistence.{CursorStream, EntityTypePersistedInfo}
import android.widget.ResourceCursorAdapter
import android.view.View
import android.content.Context
import android.database.Cursor

/**
 * A factory for an [[com.github.scrud.android.view.EntityAdapter]] or similar class.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/11/12
 * Time: 7:46 AM
 */
class EntityAdapterFactory {
  private[android] def createAdapter(persistence: CrudPersistence, contextItems: CrudContextItems, activity: Activity, itemLayout: LayoutKey): AdapterCaching = {
    val _entityType = persistence.entityType
    val entityTypePersistedInfo = EntityTypePersistedInfo(_entityType)
    val findAllResult = persistence.findAll(contextItems.currentUriPath)
    findAllResult match {
      case CursorStream(cursor, _) =>
        activity.startManagingCursor(cursor)
        val persistenceFactory = contextItems.crudContext.application.persistenceFactory(_entityType)
        val listener = new DataListener {
          def onChanged(uri: UriPath) {
            cursor.requery()
          }
        }
        persistenceFactory.addListener(listener, _entityType, contextItems.crudContext)
        new ResourceCursorAdapter(activity, itemLayout, cursor) with AdapterCaching {
          def entityType = _entityType

          /** The UriPath that does not contain the entities. */
          protected def uriPathWithoutEntityId = contextItems.currentUriPath

          def bindView(view: View, context: Context, cursor: Cursor) {
            val row = entityTypePersistedInfo.copyRowToMap(cursor)
            bindViewFromCacheOrItems(view, cursor.getPosition, row, contextItems.crudContext, contextItems)
          }
        }
      case _ => new EntityAdapter(_entityType, findAllResult, itemLayout, contextItems, activity.getLayoutInflater)
    }
  }
}
