package com.github.scrud.android.view

import com.github.scrud.{EntityName, UriField}
import com.github.scrud.platform.PlatformTypes.ID
import com.github.triangle._
import android.widget._
import android.view.View
import android.app.Activity
import xml.NodeSeq
import com.github.scrud.android.CrudActivity
import com.github.scrud.android.AndroidCrudContext
import scala.Some

/** A ViewField that allows choosing a specific entity of a given EntityType or displaying its fields' values.
  * The layout for the EntityType that contains this EntityView may refer to fields of this view's EntityType
  * in the same way as referring to its own fields.  If both have a field of the same name, the behavior is undefined.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class EntityView(entityName: EntityName)
  extends ViewField[ID](FieldLayout(displayXml = NodeSeq.Empty, editXml = <Spinner android:drawSelectorOnTop = "true"/>),
    Getter[AdapterView[BaseAdapter], ID](v => Option(v.getSelectedItemId)) + Setter[ID] {
      case UpdaterInput(adapterView: AdapterView[BaseAdapter], idOpt, UriField(Some(uri)) &&
          CrudContextField(Some(crudContext @ AndroidCrudContext(crudActivity: CrudActivity, _, _)))) =>
        if (idOpt.isDefined || adapterView.getAdapter == null) {
          //don't do it again if already done from a previous time
          if (adapterView.getAdapter == null) {
            val entityType = crudActivity.crudApplication.entityType(entityName)
            crudActivity.setListAdapter(adapterView, entityType, crudContext, crudActivity.requestContext.copy(uri), crudActivity,
              crudActivity.pickLayoutFor(entityType.entityName))
          }
          if (idOpt.isDefined) {
            val id = idOpt.get
            crudContext.runOnUiThread {
              val adapter = adapterView.getAdapter
              val position: Int = (0 to (adapter.getCount - 1)).indexWhere(adapter.getItemId(_) == id)
              if (position >= 0) {
                adapterView.setSelection(position)
              } else {
                crudContext.logger.info("Unable to set selection on adapterView=" + adapterView + " for entityName=" + entityName + " to id=" + id + " since position=" + position)
              }
            }
          }
        }
      case UpdaterInput(AndroidUIElement(uiElement), idOpt, input @ UriField(Some(baseUri)) &&
          CrudContextField(Some(crudContext @ AndroidCrudContext(crudActivity: CrudActivity, _, _)))) =>
        val uriOpt = idOpt.map(baseUri / _)
        val updaterInput = UpdaterInput(uiElement, input)
        val entityType = crudActivity.crudApplication.entityType(entityName)
        uriOpt.map(crudActivity.populateFromUri(entityType, _, updaterInput)).getOrElse {
          entityType.defaultValue.update(updaterInput)
        }
    }) {
  override lazy val toString = "EntityView(" + entityName + ")"
}

private object AndroidUIElement {
  def unapply(target: AnyRef): Option[AnyRef] = target match {
    case view: View => Some(view)
    case activity: Activity => Some(activity)
    case _ => None
  }
}
