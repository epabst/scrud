package com.github.scrud.android.view

import android.view.View
import AndroidConversions.toOnClickListener
import com.github.scrud.action.Operation
import com.github.scrud.copy.CopyContext
import scala.xml.NodeSeq
import com.github.scrud.android.AndroidCommandContext

/** A Setter that invokes an Operation when the View is clicked.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class OnClickSetterField(viewOperation: View => Operation) extends TypedViewTargetField[View,Nothing](NodeSeq.Empty) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(view: View, valueOpt: Option[Nothing], androidContext: AndroidCommandContext, context: CopyContext): View = {
    if (!view.isClickable) {
      view.setClickable(true)
    }
    if (view.isClickable) {
      view.setOnClickListener { (view: View) =>
        context.withExceptionReporting {
          viewOperation(view).invoke(context.sourceUri, androidContext)
        }
      }
    }
    view
  }
}
