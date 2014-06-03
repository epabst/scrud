package com.github.scrud.android.view

import xml.NodeSeq


/** The layout piece for a field.
  * It provides the XML for the part of an Android Layout that corresponds to a single field.
  * Standards attributes are separately added such as android:id and those needed by the parent View.
  * @author Eric Pabst (epabst@gmail.com)
  */
@deprecated("just use the xml directly in a ViewTargetField or ViewStorageField", since = "2014-04-30")
abstract class FieldLayout { self =>
  def displayXml: NodeSeq
  def editXml: NodeSeq
  /** Returns a similar FieldLayout but where the editXml is overridden to be empty. */
  lazy val suppressEdit: FieldLayout = new FieldLayout {
    val displayXml = self.displayXml
    def editXml = NodeSeq.Empty
  }
  /** Returns a similar FieldLayout but where the displayXml is overridden to be empty. */
  lazy val suppressDisplay: FieldLayout = new FieldLayout {
    def displayXml = NodeSeq.Empty
    val editXml = self.editXml
  }
}

object FieldLayout {
  def apply(displayXml: NodeSeq, editXml: NodeSeq): FieldLayout = {
    val _displayXml = displayXml
    val _editXml = editXml
    new FieldLayout {
      def displayXml = _displayXml
      def editXml = _editXml
    }
  }

  def textLayout(inputType: String) = new FieldLayout {
    val displayXml = <TextView style="@android:style/TextAppearance.Widget.TextView"/>
    val editXml = <EditText android:inputType={inputType}/>
  }

  lazy val noLayout = FieldLayout(NodeSeq.Empty, NodeSeq.Empty)
  lazy val nameLayout = textLayout("textCapWords")
  lazy val intLayout = textLayout("number|numberSigned")
  lazy val longLayout = textLayout("number|numberSigned")
  lazy val doubleLayout = textLayout("numberDecimal|numberSigned")
  lazy val currencyLayout = textLayout("numberDecimal|numberSigned")
  lazy val datePickerLayout = new FieldLayout {
    val displayXml = <TextView style="@android:style/TextAppearance.Widget.TextView"/>
    val editXml = <DatePicker/>
  }
  lazy val dateTextLayout = textLayout("date")

  private[scrud] def toDisplayName(id: String): String = {
    var makeUpperCase = true
    val displayName = id.collect {
      case c if Character.isUpperCase(c) =>
        makeUpperCase = false
        " " + c
      case '_' =>
        makeUpperCase = true
        " "
      case c if makeUpperCase =>
        makeUpperCase = false
        Character.toUpperCase(c)
      case c => c.toString
    }.mkString
    displayName.stripPrefix(" ")
  }
}
