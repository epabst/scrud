package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle._
import converter.Converter
import PortableField._
import java.util.{Calendar, Date, GregorianCalendar}
import FieldLayout._
import com.github.triangle.converter.Converter._
import android.widget._
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import android.view.View
import android.widget.LinearLayout
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.{TypedTargetField, TypedSourceField, SourceField, AdaptedField}
import com.github.scrud.context.CommandContext

/** PortableField for Views.
  * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@deprecated("use ViewTargetField", since = "2014-04-28")
class ViewField[T](val defaultLayout: FieldLayout, delegate: PortableField[T]) extends Field[T](delegate) { self =>
  lazy val suppressEdit: ViewField[T] = ViewField[T](defaultLayout.suppressEdit, this)
  lazy val suppressDisplay: ViewField[T] = ViewField[T](defaultLayout.suppressDisplay, this)

  def withDefaultLayout(newDefaultLayout: FieldLayout): ViewField[T] = ViewField[T](newDefaultLayout, this)
}

object ViewField {
  def viewId[T](viewRef: ViewRef, childViewField: PortableField[T]): PortableField[T] =
    new ViewIdField[T](viewRef, childViewField).withViewKeyMapField + defaultToNone

  def viewId[T](viewResourceId: ViewKey, childViewField: PortableField[T]): PortableField[T] =
    viewId(ViewRef(viewResourceId), childViewField)

  /** This should be used when R.id doesn't yet have the needed name, and used like this:
    * {{{viewId(classOf[R.id], "name", ...)}}}
    * Which is conceptually identical to
    * {{{viewId(R.id.name, ...)}}}.
    */
  def viewId[T](rIdClass: Class[_], viewResourceIdName: String, childViewField: PortableField[T]): PortableField[T] =
    viewId(ViewRef(viewResourceIdName, detectRIdClasses(rIdClass)), childViewField)

  def apply[T](defaultLayout: FieldLayout, dataField: PortableField[T]): ViewField[T] = new ViewField[T](defaultLayout, dataField)

  val textView: ViewField[String] = new ViewField[String](nameLayout, ViewGetter[TextView,String] { v =>
    toOption(v.getText.toString.trim)
  }.withSetter {v => valueOpt =>
    v.setText(valueOpt.getOrElse(""))
  }) {
    override val toString = "textView"
  }
  def formattedTextView[T](toDisplayString: Converter[T,String], toEditString: Converter[T,String],
                           fromString: Converter[String,T], defaultLayout: FieldLayout = nameLayout): ViewField[T] =
    new ViewField[T](defaultLayout, Getter[TextView,T](view => toOption(view.getText.toString.trim).flatMap(fromString.convert(_))) +
        Setter[T] {
          case UpdaterInput(view: EditText, valueOpt, CommandContextField(commandContext: AndroidCommandContext)) => {
            val text = valueOpt.flatMap(toEditString.convert(_)).getOrElse("")
            commandContext.runOnUiThread {
              view.setText(text)
            }
          }
          case UpdaterInput(view: TextView, valueOpt, CommandContextField(commandContext: AndroidCommandContext)) => {
            val text = valueOpt.flatMap(toDisplayString.convert(_)).getOrElse("")
            commandContext.runOnUiThread {
              view.setText(text)
            }
          }
        }
    ) {
      override val toString = "formattedTextView"
    }
  def textViewWithInputType(inputType: String): ViewField[String] = textView.withDefaultLayout(textLayout(inputType))
  lazy val phoneView: ViewField[String] = textViewWithInputType("phone")
  lazy val doubleView: ViewField[Double] = ViewField[Double](doubleLayout, formatted(textView))
  lazy val percentageView: ViewField[Float] = formattedTextView[Float](percentageToString, percentageToEditString, stringToPercentage, doubleLayout)
  lazy val viewWeightField: Setter[Float] =
    ViewSetter { (view: View) => (valueOpt: Option[Float]) =>
      val oldLayoutParams = view.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
      val newLayoutParams = new LinearLayout.LayoutParams(oldLayoutParams.width, oldLayoutParams.height, valueOpt.getOrElse(0.0f))
      view.setLayoutParams(newLayoutParams)
    }
  lazy val currencyView = formattedTextView[Double](currencyToString, currencyToEditString, stringToCurrency, currencyLayout)
  lazy val intView: ViewField[Int] = ViewField[Int](intLayout, formatted[Int](textView))
  lazy val longView: ViewField[Long] = ViewField[Long](longLayout, formatted[Long](textView))

  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)

  private val calendarPickerField = ViewGetter[DatePicker,Calendar] {
    (p: DatePicker) => Some(new GregorianCalendar(p.getYear, p.getMonth, p.getDayOfMonth))
  }.withSetter { (picker: DatePicker) => valueOpt =>
    val calendar = valueOpt.getOrElse(Calendar.getInstance())
    picker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
  }

  implicit val dateView: ViewField[Date] = new ViewField[Date](datePickerLayout,
    formattedTextView(dateToDisplayString, dateToString, stringToDate) +
        converted(dateToCalendar, calendarPickerField, calendarToDate)) {
    override val toString = "dateView"
  }

  val calendarDateView: ViewField[Calendar] = new ViewField[Calendar](datePickerLayout, converted(calendarToDate, dateView, dateToCalendar)) {
    override val toString = "calendarDateView"
  }

  @deprecated("use EnumerationView")
  def enumerationView[E <: Enumeration#Value](enum: Enumeration): ViewField[E] = EnumerationView[E](enum)
}
