package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import android.view.View
import org.junit.Test
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.github.scrud.{FieldName, EntityType, UriPath, EntityName}
import android.widget._
import java.util.Locale
import android.content.Context
import com.github.scrud.android._
import org.mockito.Matchers
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import scala.reflect.Manifest
import com.github.scrud.copy.CopyContext
import scala.Some
import com.github.scrud.android.testres.R
import com.github.scrud.persistence.{PersistenceFactory, EntityTypeMapForTesting}
import com.github.scrud.types.StringConvertibleQT
import scala.util.{Success, Try}
import com.github.scrud.platform.representation.{EditUI, SummaryUI}

/**
 * A behavior specification for Android EditUI and DisplayUI fields.
 * @author Eric Pabst (epabst@gmail.com)
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class ViewFieldSpec extends MustMatchers with MockitoSugar {
  class MyEntity(var string: String, var number: Int)
  val context = mock[Context]
  val itemLayoutId = android.R.layout.simple_spinner_dropdown_item
  Locale.setDefault(Locale.US)
  private val platformDriver = new AndroidPlatformDriver(classOf[R])
  val application = new CrudApplicationForTesting(platformDriver, EntityTypeMapForTesting(Map.empty[EntityType,PersistenceFactory]))

  val stringConvertibleType = new StringConvertibleQT[String] {
    /** Convert the value to a String for display. */
    override def convertToDisplayString(value: String): String = value + " on display"

    /** Convert the value to a String for editing.  This may simply call convertToString(value). */
    override def convertToString(value: String): String = value + " to edit"

    /** Convert the value from a String (whether for editing or display. */
    override def convertFromString(string: String): Try[String] = Success("parsed " + string)
  }

  class ViewForTesting(context: Context, var status: String) extends View(context)

  @Test
  def itMustClearTheViewIfEmpty() {
    val viewGroup = mock[View]
    val view1 = mockView[TextView]
    val view2 = mockView[TextView]
    val view3 = mockView[TextView]
    stub(viewGroup.findViewById(101)).toReturn(view1)
    stub(viewGroup.findViewById(102)).toReturn(view2)
    stub(viewGroup.findViewById(103)).toReturn(view3)
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    viewField.toAdaptableField.findTargetField(EditUI).get.updateValue(viewGroup, None, new CopyContext(UriPath.EMPTY, null))
    verify(view1).setText("")
  }

  private def mockView[T <: View](implicit manifest: Manifest[T]): T = {
    val view = mock[T]
    when(view.post(Matchers.any())).thenAnswer(new Answer[Boolean] {
      def answer(p1: InvocationOnMock) = {
        val runnable = p1.getArguments.apply(0).asInstanceOf[Runnable]
        runnable.run()
        true
      }
    })
    view
  }

  @Test
  def itMustOnlyCopyToAndFromViewByIdIfIdIsFound() {
    val viewGroup = mock[View]
    stub(viewGroup.findViewById(Matchers.anyInt())).toReturn(null)
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val targetField = viewField.toAdaptableField.findTargetField(EditUI)
    val copyContext = new CopyContext(UriPath.EMPTY, null)
    targetField.get.updateValue(viewGroup, None, copyContext)
  }

  @Test
  def itMustConvertNullToNone() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val view = new TextView(context)
    view.setText(null)
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, null)) must be (None)
  }

  @Test
  def itMustConvertEmptyStringToNone() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val view = new TextView(context)
    view.setText("")
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, null)) must be (None)
  }

  @Test
  def itMustTrimStrings() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val view = new TextView(context)
    view.setText(" hello world ")
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, null)) must be (Some("hello world"))

    view.setText(" ")
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, null)) must be (None)
  }

  @Test
  def formattedTextViewMustUseToEditStringConverterForEditTextView() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val editView = mockView[EditText]
    viewField.toAdaptableField.findTargetField(EditUI).get.updateValue(editView, Some("marbles"), new CopyContext(UriPath.EMPTY, null))
    verify(editView).setText("marbles to edit")
  }

  @Test
  def formattedTextViewMustUseToDisplayStringConverterForTextView() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val textView = mockView[TextView]
    viewField.toAdaptableField.findTargetField(SummaryUI).get.updateValue(textView, Some("marbles"), new CopyContext(UriPath.EMPTY, null))
    verify(textView).setText("marbles on display")
  }

  @Test
  def formattedTextViewMustTrimAndUseFromStringConverterWhenGetting() {
    val viewField = platformDriver.field(EntityName("foo"), FieldName("bar"), stringConvertibleType, Seq(SummaryUI, EditUI))
    val textView = mockView[TextView]
    stub(textView.getText).toReturn("  given text   ")
    viewField.toAdaptableField.findSourceField(EditUI).get.findValue(textView, new CopyContext(UriPath.EMPTY, null)) must be (Some("parsed given text"))
  }
}
