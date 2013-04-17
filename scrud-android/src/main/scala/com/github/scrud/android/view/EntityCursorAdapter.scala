package com.github.scrud.android.view

import android.support.v4.widget.ResourceCursorAdapter
import com.github.scrud.android.persistence.EntityTypePersistedInfo
import android.content.Context
import android.database.Cursor
import com.github.scrud.{CrudContextItems, EntityType}
import android.view.View
import com.github.scrud.android.AndroidCrudContext

/**
 * A CursorAdapter that uses a given entityType and ViewInflater.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/16/13
 *         Time: 11:09 PM
 */
class EntityCursorAdapter(val entityType: EntityType, contextItems: CrudContextItems, itemViewInflater: ViewInflater, cursor: Cursor)
    extends ResourceCursorAdapter(contextItems.crudContext.asInstanceOf[AndroidCrudContext].context, itemViewInflater.viewKey, cursor, 0) with AdapterCaching {
  val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)

  /** The UriPath that does not contain the entities. */
  protected def uriPathWithoutEntityId = contextItems.currentUriPath

  def bindView(view: View, context: Context, cursor: Cursor) {
    val row = entityTypePersistedInfo.copyRowToMap(cursor)
    bindViewFromCacheOrItems(view, cursor.getPosition, row, contextItems)
  }
}
