package com.github.scrud.android.view

import android.widget.DatePicker
import java.util.{GregorianCalendar, Calendar}
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext

/**
 * ViewStorageField for a [[android.widget.DatePicker]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class DatePickerField extends ViewStorageField[DatePicker,Calendar](<DatePicker/>) {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(datePicker: DatePicker, valueOpt: Option[Calendar], androidcontext: AndroidCommandContext, context: CopyContext) = {
    val calendar = valueOpt.getOrElse(Calendar.getInstance())
    datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    datePicker
  }

  /** Get some value or None from the given source. */
  def findFieldValue(datePicker: DatePicker, context: AndroidCommandContext) = {
    Some(new GregorianCalendar(datePicker.getYear, datePicker.getMonth, datePicker.getDayOfMonth))
  }
}
