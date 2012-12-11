package com.github.scrud.android.view

import com.github.scrud.{CrudContextField, UriField, EntityType}
import com.github.scrud.platform.PlatformTypes.ID
import com.github.triangle._
import android.widget._
import android.view.View
import android.app.Activity
import xml.NodeSeq
import com.github.scrud.android.BaseCrudActivity
import com.github.scrud.android.AndroidCrudContext
import scala.Some

/** A ViewField that allows choosing a specific entity of a given EntityType or displaying its fields' values.
  * The layout for the EntityType that contains this EntityView may refer to fields of this view's EntityType
  * in the same way as referring to its own fields.  If both have a field of the same name, the behavior is undefined.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class EntityView(entityType: EntityType)
  extends ViewField[ID](FieldLayout(displayXml = NodeSeq.Empty, editXml = <Spinner android:drawSelectorOnTop = "true"/>)) {

  private object AndroidUIElement {
    def unapply(target: AnyRef): Option[AnyRef] = target match {
      case view: View => Some(view)
      case activity: Activity => Some(activity)
      case _ => None
    }
  }

  protected val delegate = Getter[AdapterView[BaseAdapter], ID](v => Option(v.getSelectedItemId)) + Setter[ID] {
    case UpdaterInput(adapterView: AdapterView[BaseAdapter], idOpt, UriField(Some(uri)) && CrudContextField(Some(crudContext @ AndroidCrudContext(crudActivity: BaseCrudActivity, _)))) =>
      if (idOpt.isDefined || adapterView.getAdapter == null) {
        //don't do it again if already done from a previous time
        if (adapterView.getAdapter == null) {
          crudActivity.setListAdapter(adapterView, entityType, uri, crudContext, crudActivity.contextItems, crudActivity,
            crudActivity.pickLayoutFor(entityType.entityName))
        }
        if (idOpt.isDefined) {
          val adapter = adapterView.getAdapter
          val position = (0 to (adapter.getCount - 1)).view.map(adapter.getItemId(_)).indexOf(idOpt.get)
          adapterView.setSelection(position)
        }
      }
    case UpdaterInput(AndroidUIElement(uiElement), idOpt, input @ UriField(Some(baseUri)) && CrudContextField(Some(crudContext @ AndroidCrudContext(crudActivity: BaseCrudActivity, _)))) =>
      val uriOpt = idOpt.map(baseUri / _)
      val updaterInput = UpdaterInput(uiElement, input)
      uriOpt.map(crudActivity.populateFromUri(entityType, _, updaterInput)).getOrElse {
        entityType.defaultValue.update(updaterInput)
      }
  }

  override lazy val toString = "EntityView(" + entityType.entityName + ")"
}
