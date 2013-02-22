package com.github.scrud.android.view

import com.github.triangle.PortableField._
import com.github.triangle.converter.ValueFormat._
import android.widget.{AdapterView, ArrayAdapter, BaseAdapter}
import com.github.triangle.Getter
import scala.collection.JavaConversions._

/** A ViewField for an [[scala.Enumeration]].
  * @author Eric Pabst (epabst@gmail.com)
  */
case class EnumerationView[E <: Enumeration#Value](enum: Enumeration)
  extends ViewField[E](FieldLayout(displayXml = <TextView style="@android:style/TextAppearance.Widget.TextView"/>, editXml = <Spinner android:drawSelectorOnTop = "true"/>)) {

  private val itemViewResourceId = _root_.android.R.layout.simple_spinner_dropdown_item
  private val valueArray: java.util.List[E] = enum.values.toSeq.map(_.asInstanceOf[E])

  protected val delegate = Getter[AdapterView[BaseAdapter],E](v => Option(v.getSelectedItem.asInstanceOf[E])).
    withSetter { adapterView => valueOpt =>
      //don't do it again if already done from a previous time
      if (adapterView.getAdapter == null) {
        val adapter = new ArrayAdapter[E](adapterView.getContext, itemViewResourceId, valueArray)
        adapterView.setAdapter(adapter)
      }
      adapterView.setSelection(valueOpt.map(valueArray.indexOf(_)).getOrElse(AdapterView.INVALID_POSITION))
    } + formatted[E](enumFormat(enum), ViewField.textView)

  override lazy val toString = "EnumerationView(" + enum.getClass.getSimpleName + ")"
}
