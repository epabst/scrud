package com.github.scrud.android.view

import android.widget.EditText
import scala.xml.NodeSeq
import com.github.scrud.types.StringConvertibleQT
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext

/**
 * TargetField for an Android EditText view.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class EditTextField[V](stringConvertible: StringConvertibleQT[V], defaultLayout: NodeSeq)
    extends TypedViewStorageField[EditText,V](defaultLayout) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(editTextView: EditText, valueOpt: Option[V], commandContext: AndroidCommandContext, context: CopyContext) = {
    val charSequence = valueOpt.fold("")(stringConvertible.convertToString(_))
    editTextView.setText(charSequence)
    editTextView
  }

  /** Get some value or None from the given source. */
  def findFieldValue(editTextView: EditText, context: AndroidCommandContext) =
    toOption(editTextView.getText).flatMap { charSequence =>
      stringConvertible.convertFromString(charSequence.toString).toOption
    }

  private def toOption(charSequence: CharSequence): Option[CharSequence] =
    if (charSequence.length() == 0) None else Some(charSequence)
}
