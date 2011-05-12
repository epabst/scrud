package com.github.scala_android.crud

import res.R
import android.widget.BaseAdapter
import android.view.{ViewGroup, View}
import android.app.Activity
import com.github.triangle.PortableField.identityField

trait GeneratedCrudType[T <: AnyRef] extends CrudType {
  def newWritable = throw new UnsupportedOperationException("not supported")

  def openEntityPersistence(crudContext: CrudContext): ListEntityPersistence[T]

  def createListAdapter(persistence: CrudPersistence, crudContext: CrudContext, activity: Activity) = new BaseAdapter() {
    val listPersistence = persistence.asInstanceOf[ListEntityPersistence[T]]
    val list: List[T] = {
      val criteria = listPersistence.newCriteria
      copyFields(activity.getIntent, criteria)
      listPersistence.findAll(criteria)
    }

    def getCount: Int = list.size

    def getItemId(position: Int) = listPersistence.getId(list(position))

    def getItem(position: Int) = list(position)

    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val contextItems = List(activity.getIntent, crudContext, Unit)
      val view = if (convertView == null) activity.getLayoutInflater.inflate(rowLayout, parent, false) else convertView
      copyFieldsFromItem(list(position) :: contextItems, view)
      view
    }
  }

  def refreshAfterSave(crudContext: CrudContext) {}

  override def getListActions(actionFactory: UIActionFactory) = foreignKeys match {
    //exactly one parent w/o a display page
    case foreignKey :: Nil if !foreignKey.entityType.hasDisplayPage => {
      val parentEntity = foreignKey.entityType
      val getForeignKey = { _: Unit => foreignKey(actionFactory.currentIntent) }
      actionFactory.adapt(actionFactory.startUpdate(parentEntity), getForeignKey) ::
              parentEntity.displayChildEntityLists(actionFactory, getForeignKey, childEntities(actionFactory.application))
    }
    case _ => Nil
  }

  override def getEntityActions(actionFactory: UIActionFactory): List[UIAction[ID]] =
    displayLayout.map(_ => actionFactory.display(this)).toList

  override val cancelItemString = R.string.cancel_item
}

object GeneratedCrudType {
  val crudContextField = identityField[CrudContext]
}