package com.github.scrud.android.view

import android.widget.BaseAdapter
import com.github.scrud.platform.PlatformTypes._
import android.view.{ViewGroup, View}
import com.github.scrud
import scrud.android.AndroidCrudContext
import scrud.persistence.RefreshableFindAll
import scrud.{CrudContextItems, CrudContextField, EntityType}

/** An Android Adapter for an EntityType with the result of EntityPersistence.findAll.
  * @author Eric Pabst (epabst@gmail.com)
  */
class EntityAdapter(val entityType: EntityType, refreshableFindAll: RefreshableFindAll, rowViewInflater: ViewInflater,
                    contextItems: CrudContextItems) extends BaseAdapter with AdapterCaching {
  private def values: Seq[AnyRef] = refreshableFindAll.currentResults

  /** The UriPath that does not contain the id of the entities. */
  protected lazy val uriPathWithoutEntityId = contextItems.currentUriPath

  protected lazy val crudContext =
    CrudContextField(contextItems).getOrElse(sys.error("no CrudContext found")).asInstanceOf[AndroidCrudContext]

  def getItemId(position: Int): ID = getItemId(getItem(position), position)

  // Not a val because values may or may not be immutable
  def getCount: Int = values.size

  def getItem(position: Int) = values(position)

  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = if (convertView == null) rowViewInflater.inflate(parent) else convertView
    bindViewFromCacheOrItems(view, position, contextItems)
    view
  }
}
