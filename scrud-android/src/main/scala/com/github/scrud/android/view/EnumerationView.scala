package com.github.scrud.android.view

import android.widget.{AdapterView, ArrayAdapter, BaseAdapter}
import scala.collection.JavaConversions._
import com.github.scrud.android.AndroidCommandContext

/**
 * A ViewStorageField for an [[scala.Enumeration]].
 * @author Eric Pabst (epabst@gmail.com)
 */
case class EnumerationView[E <: Enumeration#Value](enum: Enumeration)
  extends ViewStorageField[AdapterView[BaseAdapter],E](<Spinner android:drawSelectorOnTop = "true"/>) {

  private val valueArray: java.util.List[E] = enum.values.toSeq.map(_.asInstanceOf[E])
  private val itemViewResourceId = _root_.android.R.layout.simple_spinner_dropdown_item

  /** Get some value or None from the given source. */
  override def findFieldValue(adapterView: AdapterView[BaseAdapter], context: AndroidCommandContext): Option[E] = {
    Option(adapterView.getSelectedItem.asInstanceOf[E])
  }

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(adapterView: AdapterView[BaseAdapter], valueOpt: Option[E], context: AndroidCommandContext): AdapterView[BaseAdapter] = {
    //don't do it again if already done from a previous time
    if (adapterView.getAdapter == null) {
      val adapter = new ArrayAdapter[E](adapterView.getContext, itemViewResourceId, valueArray)
      adapterView.setAdapter(adapter)
    }
    adapterView.setSelection(valueOpt.fold(AdapterView.INVALID_POSITION)(valueArray.indexOf(_)))
    adapterView
  }

  override lazy val toString = "EnumerationView(" + enum.getClass.getSimpleName + ")"
}
