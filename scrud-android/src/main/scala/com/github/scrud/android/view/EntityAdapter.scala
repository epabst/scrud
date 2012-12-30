package com.github.scrud.android.view

import android.widget.BaseAdapter
import com.github.scrud.platform.PlatformTypes._
import android.view.{ViewGroup, View}
import com.github.scrud
import scrud.android.AndroidCrudContext
import scrud.{CrudContextItems, CrudContextField, UriField, EntityType}

/** An Android Adapter for an EntityType with the result of EntityPersistence.findAll.
  * @author Eric Pabst (epabst@gmail.com)
  */
class EntityAdapter(val entityType: EntityType, values: Seq[AnyRef], rowViewInflater: ViewInflater,
                    contextItems: CrudContextItems) extends BaseAdapter with AdapterCaching {

  /** The UriPath that does not contain the entities. */
  protected lazy val uriPathWithoutEntityId = UriField(contextItems).getOrElse(sys.error("no UriPath provided"))

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
