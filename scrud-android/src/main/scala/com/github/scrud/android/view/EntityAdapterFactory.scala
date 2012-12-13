package com.github.scrud.android.view

import com.github.scrud.persistence.{DataListener, CrudPersistence}
import com.github.scrud.{UriPath, CrudContextItems}
import android.app.Activity
import com.github.scrud.android.persistence.{CursorStream, EntityTypePersistedInfo}
import android.widget.ResourceCursorAdapter
import android.view.View
import android.content.Context
import android.database.Cursor
import com.github.scrud.android.AndroidCrudContext

/**
 * A factory for an [[com.github.scrud.android.view.EntityAdapter]] or similar class.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/11/12
 * Time: 7:46 AM
 */
class EntityAdapterFactory {
  private[android] def createAdapter(persistence: CrudPersistence, contextItems: CrudContextItems, itemViewInflater: ViewInflater): AdapterCaching = {
    val _entityType = persistence.entityType
    val entityTypePersistedInfo = EntityTypePersistedInfo(_entityType)
    val findAllResult = persistence.findAll(contextItems.currentUriPath)
    findAllResult match {
      case CursorStream(cursor, _) =>
        val activity: Activity = getActivity(contextItems)
        activity.startManagingCursor(cursor)
        val persistenceFactory = persistence.persistenceFactory
        val listener = new DataListener {
          def onChanged(uri: UriPath) {
            cursor.requery()
          }
        }
        persistenceFactory.addListener(listener, _entityType, contextItems.crudContext)
        new ResourceCursorAdapter(activity, itemViewInflater.viewKey, cursor) with AdapterCaching {
          def entityType = _entityType

          /** The UriPath that does not contain the entities. */
          protected def uriPathWithoutEntityId = contextItems.currentUriPath

          def bindView(view: View, context: Context, cursor: Cursor) {
            val row = entityTypePersistedInfo.copyRowToMap(cursor)
            bindViewFromCacheOrItems(view, cursor.getPosition, row, contextItems)
          }
        }
      case _ => new EntityAdapter(_entityType, findAllResult, itemViewInflater, contextItems)
    }
  }

  private[android] def getActivity(contextItems: CrudContextItems): Activity = {
    val activity = contextItems.crudContext.asInstanceOf[AndroidCrudContext].activityContext.asInstanceOf[Activity]
    activity
  }
}
