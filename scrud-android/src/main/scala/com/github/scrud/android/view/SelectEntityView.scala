package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes.ID
import com.github.scrud.EntityName
import com.github.scrud.context.CommandContext
import android.view.View
import com.github.scrud.platform.representation.SelectUI
import com.github.scrud.copy.SourceType
import android.widget.{BaseAdapter, AdapterView}
import com.github.scrud.android.AndroidCommandContext

/**
 * A [[com.github.scrud.android.view.ViewStorageField]] for allowing a user to select an entity from a list.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/6/14
 *         Time: 6:35 AM
 */
case class SelectEntityView(entityName: EntityName)
    extends ViewStorageField[AdapterView[BaseAdapter],ID](<Spinner android:drawSelectorOnTop = "true"/>) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(adapterView: AdapterView[BaseAdapter], idOpt: Option[ID], context: AndroidCommandContext) = {
    val entityType = context.entityTypeMap.entityType(entityName)

    if (idOpt.isDefined || adapterView.getAdapter == null) {
      //don't do it again if already done from a previous time
      if (adapterView.getAdapter == null) {
        context.crudActivity.setListAdapter(adapterView, entityType, context, context.copy(uri), crudActivity,
          crudActivity.pickLayoutFor(entityType.entityName))
      }
      if (idOpt.isDefined) {
        val id = idOpt.get
        context.runOnUiThread {
          val adapter = adapterView.getAdapter
          val position: Int = (0 to (adapter.getCount - 1)).indexWhere(adapter.getItemId(_) == id)
          if (position >= 0) {
            adapterView.setSelection(position)
          } else {
            context.platformDriver.info("Unable to set selection on adapterView=" + adapterView + " for entityName=" + entityName + " to id=" + id + " since position=" + position)
          }
        }
      }
    }
    val uri = entityName.toUri(idOpt)
    context.findOrEmpty(uri, SelectUI, adapterView)
  }

  /** Get some value or None from the given source. */
  def findFieldValue(adapterView: AdapterView[BaseAdapter], context: AndroidCommandContext) = {
    val id = adapterView.getSelectedItemId
    if (id >= 0) Some(id) else None
  }
}
