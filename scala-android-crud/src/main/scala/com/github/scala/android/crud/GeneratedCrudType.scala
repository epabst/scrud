package com.github.scala.android.crud

import common.UriPath
import android.view.{ViewGroup, View}
import com.github.triangle.PortableField.identityField
import android.app.ListActivity
import android.widget.{ListAdapter, BaseAdapter}
import com.github.triangle.Field
import common.PlatformTypes._

trait GeneratedCrudType[T <: AnyRef] extends CrudType {
  def newWritable = throw new UnsupportedOperationException("not supported")

  protected def createEntityPersistence(crudContext: CrudContext): SeqCrudPersistence[T]

  class SeqPersistenceAdapter[T <: AnyRef](findAllResult: Seq[AnyRef], contextItems: List[AnyRef], activity: ListActivity)
          extends BaseAdapter with AdapterCaching {
    val seq: Seq[T] = findAllResult.asInstanceOf[Seq[T]]

    def getCount: Int = seq.size

    def getItemId(position: Int): ID = getItem(position) match {
      case IdField(Some(id)) => id
      case _ => position
    }

    def getItem(position: Int) = seq(position)

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val view = if (convertView == null) activity.getLayoutInflater.inflate(rowLayout, parent, false) else convertView
      bindViewFromCacheOrItems(view, getItem(position) :: contextItems, position, activity)
      view
    }
  }

  def setListAdapter(findAllResult: Seq[AnyRef], contextItems: List[AnyRef], activity: CrudListActivity) {
    activity.setListAdapter(new SeqPersistenceAdapter[T](findAllResult, contextItems, activity))
  }

  def refreshAfterDataChanged(listAdapter: ListAdapter) {}

  override def getListActions(application: CrudApplication) = super.getReadOnlyListActions(application)

  override def getEntityActions(application: CrudApplication) = super.getReadOnlyEntityActions(application)
}

object GeneratedCrudType {
  object CrudContextField extends Field(identityField[CrudContext])
  object UriField extends Field(identityField[UriPath])
}