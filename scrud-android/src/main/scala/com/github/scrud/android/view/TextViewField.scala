package com.github.scrud.android.view

import android.widget.TextView
import scala.xml.NodeSeq
import com.github.scrud.types.StringConvertibleQT
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext

/**
 * ViewTargetField for an Android TextView.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class TextViewField[V](stringConvertibleType: StringConvertibleQT[V], defaultLayout: NodeSeq)
    extends TypedViewTargetField[TextView,V](defaultLayout) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(textView: TextView, valueOpt: Option[V], commandContext: AndroidCommandContext, context: CopyContext) = {
    val charSequence = valueOpt.fold("")(stringConvertibleType.convertToDisplayString(_))
    textView.setText(charSequence)
    textView
  }
}
