package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle._
import PortableField._
import java.util.{Calendar, Date, GregorianCalendar}
import FieldLayout._
import com.github.triangle.Converter._
import android.widget._
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import android.view.View
import android.widget.LinearLayout

/** A Map of ViewKey with values.
  * Wraps a map so that it is distinguished from persisted fields.
  */
case class ViewKeyMap(map: Map[ViewKey,Option[Any]]) {
  def contains(key: ViewKey) = map.contains(key)
  def apply(key: ViewKey) = map.apply(key)
  def get(key: ViewKey) = map.get(key)
  def iterator = map.iterator
  def -(key: ViewKey) = ViewKeyMap(map - key)
  def +[B1 >: Any](kv: (ViewKey, Option[B1])) = ViewKeyMap(map + kv)
}

object ViewKeyMap {
  val empty = ViewKeyMap()
  def apply(elems: (ViewKey,Option[Any])*): ViewKeyMap = new ViewKeyMap(Map(elems: _*))
}

/** An extractor to get the View from the items being copied from. */
object ViewExtractor extends Field(identityField[View])

/** PortableField for Views.
  * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class ViewField[T](val defaultLayout: FieldLayout) extends DelegatingPortableField[T] { self =>
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

  def apply[T](defaultLayout: FieldLayout, dataField: PortableField[T]): ViewField[T] = new ViewField[T](defaultLayout) {
    protected def delegate = dataField
  }

  val textView: ViewField[String] = new ViewField[String](nameLayout) {
    val delegate = Getter[TextView,String] { v =>
      toOption(v.getText.toString.trim)
    }.withSetter({v => value =>
      v.setText(value)
    }, _.setText(""))

    override val toString = "textView"
  }
  def formattedTextView[T](toDisplayString: Converter[T,String], toEditString: Converter[T,String],
                           fromString: Converter[String,T], defaultLayout: FieldLayout = nameLayout): ViewField[T] =
    new ViewField[T](defaultLayout) {
      val delegate = Getter[TextView,T](view => toOption(view.getText.toString.trim).flatMap(fromString.convert(_))) +
        Setter[T] {
          case UpdaterInput(view: EditText, valueOpt, _) => view.setText(valueOpt.flatMap(toEditString.convert(_)).getOrElse(""))
          case UpdaterInput(view: TextView, valueOpt, _) => view.setText(valueOpt.flatMap(toDisplayString.convert(_)).getOrElse(""))
        }

      override val toString = "formattedTextView"
    }
  def textViewWithInputType(inputType: String): ViewField[String] = textView.withDefaultLayout(textLayout(inputType))
  lazy val phoneView: ViewField[String] = textViewWithInputType("phone")
  lazy val doubleView: ViewField[Double] = ViewField[Double](doubleLayout, formatted(textView))
  lazy val percentageView: ViewField[Float] = formattedTextView[Float](percentageToString, percentageToEditString, stringToPercentage, doubleLayout)
  lazy val viewWeightField: Setter[Float] =
    Setter((view: View) => (valueOpt: Option[Float]) => {
      val oldLayoutParams = view.getLayoutParams.asInstanceOf[LinearLayout.LayoutParams]
      val newLayoutParams = new LinearLayout.LayoutParams(oldLayoutParams.width, oldLayoutParams.height, valueOpt.getOrElse(0.0f))
      view.setLayoutParams(newLayoutParams)
    })
  lazy val currencyView = formattedTextView[Double](currencyToString, currencyToEditString, stringToCurrency, currencyLayout)
  lazy val intView: ViewField[Int] = ViewField[Int](intLayout, formatted[Int](textView))
  lazy val longView: ViewField[Long] = ViewField[Long](longLayout, formatted[Long](textView))

  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)

  private val calendarPickerField = Setter[Calendar] {
    case UpdaterInput(picker: DatePicker, valueOpt, _) =>
      val calendar = valueOpt.getOrElse(Calendar.getInstance())
      picker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
  } + Getter((p: DatePicker) => Some(new GregorianCalendar(p.getYear, p.getMonth, p.getDayOfMonth)))

  implicit val dateView: ViewField[Date] = new ViewField[Date](datePickerLayout) {
    val delegate = formattedTextView(dateToDisplayString, dateToString, stringToDate) +
      converted(dateToCalendar, calendarPickerField, calendarToDate)
    override val toString = "dateView"
  }

  val calendarDateView: ViewField[Calendar] = new ViewField[Calendar](datePickerLayout) {
    val delegate = converted(calendarToDate, dateView, dateToCalendar)
    override val toString = "calendarDateView"
  }

  @deprecated("use EnumerationView")
  def enumerationView[E <: Enumeration#Value](enum: Enumeration): ViewField[E] = EnumerationView[E](enum)
}
