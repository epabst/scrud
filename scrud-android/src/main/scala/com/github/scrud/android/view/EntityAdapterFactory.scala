package com.github.scrud.android.view

import com.github.scrud.persistence.{DataListener, CrudPersistence}
import com.github.scrud.{UriPath, CrudContextItems}
import android.app.Activity
import com.github.scrud.android.persistence.{RefreshableFindAllWithCursor, EntityTypePersistedInfo}
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
    val refreshableFindAll = persistence.refreshableFindAll(contextItems.currentUriPath)
    val findAllResults = refreshableFindAll.currentResults
    if (findAllResults.isEmpty) {
      persistence.warn("No results found for entityType=" + _entityType + " uri=" + contextItems.currentUriPath)
    } else {
      persistence.debug("found count=" + findAllResults.length + " for entityType=" + _entityType + " for uri=" + contextItems.currentUriPath)
    }
    val adapter = refreshableFindAll match {
      case RefreshableFindAllWithCursor(_, cursor, _) =>
        val activity: Activity = getActivity(contextItems)
        activity.startManagingCursor(cursor)
        new ResourceCursorAdapter(activity, itemViewInflater.viewKey, cursor) with AdapterCaching {
          def entityType = _entityType

          /** The UriPath that does not contain the entities. */
          protected def uriPathWithoutEntityId = contextItems.currentUriPath

          def bindView(view: View, context: Context, cursor: Cursor) {
            val row = entityTypePersistedInfo.copyRowToMap(cursor)
            bindViewFromCacheOrItems(view, cursor.getPosition, row, contextItems)
          }
        }
      case _ => new EntityAdapter(_entityType, refreshableFindAll, itemViewInflater, contextItems)
    }
    val listener = new DataListener {
      def onChanged(uri: UriPath) {
        refreshableFindAll.refresh()
        adapter.notifyDataSetChanged()
      }
    }
    val persistenceFactory = contextItems.persistenceFactory(_entityType.entityName)
    persistenceFactory.addListener(listener, _entityType, contextItems.crudContext)

    adapter
  }

  private[android] def getActivity(contextItems: CrudContextItems): Activity = {
    contextItems.crudContext.asInstanceOf[AndroidCrudContext].activity
  }
}
