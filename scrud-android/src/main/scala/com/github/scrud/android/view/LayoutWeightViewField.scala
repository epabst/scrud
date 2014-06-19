package com.github.scrud.android.view

import scala.xml.NodeSeq
import android.view.View
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext
import android.widget.LinearLayout

/**
 * TargetField that set the layout weight.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class LayoutWeightViewField(defaultLayout: NodeSeq)
    extends TypedViewTargetField[View,Float](defaultLayout) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(view: View, valueOpt: Option[Float], commandContext: AndroidCommandContext, context: CopyContext): View = {
    val oldLayoutParams = view.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
    val newLayoutParams = new LinearLayout.LayoutParams(oldLayoutParams.width, oldLayoutParams.height, valueOpt.getOrElse(0.0f))
    view.setLayoutParams(newLayoutParams)
    view
  }
}
