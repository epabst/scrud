package com.github.scrud.android.view

import android.widget.TextView
import scala.xml.NodeSeq
import com.github.scrud.types.StringConvertibleQT
import com.github.scrud.android.AndroidCommandContext

/**
 * ViewStorageField for an Android TextView.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class TextViewField[V](stringConvertibleType: StringConvertibleQT[V], defaultLayout: NodeSeq)
    extends ViewTargetField[TextView,V](defaultLayout) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(textView: TextView, valueOpt: Option[V], context: AndroidCommandContext) = {
    val charSequence = valueOpt.fold("")(stringConvertibleType.convertToString(_))
    textView.setText(charSequence)
    textView
  }
}
