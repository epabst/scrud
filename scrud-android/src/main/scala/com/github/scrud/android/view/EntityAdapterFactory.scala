package com.github.scrud.android.view

import com.github.scrud.persistence.{DataListener, CrudPersistence}
import com.github.scrud.CrudContextItems
import android.app.Activity
import com.github.scrud.android.persistence.RefreshableFindAllWithCursor
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
        new EntityCursorAdapter(_entityType, contextItems, itemViewInflater, cursor)
      case _ => new EntityAdapter(_entityType, refreshableFindAll, itemViewInflater, contextItems)
    }
    val listener = new DataListener {
      def onChanged() {
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
