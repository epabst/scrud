package com.github.scrud.android.view

import org.junit.runner.RunWith
import _root_.android.view.View
import org.junit.Test
import org.mockito.Mockito._
import _root_.android.widget._
import java.util.Locale
import _root_.android.content.{Intent, Context}
import com.github.scrud.android._
import org.mockito.Matchers
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock
import scala.reflect.Manifest
import com.github.scrud.copy.CopyContext
import com.github.scrud.types.{TitleQT, StringConvertibleQT}
import scala.util.Try
import com.github.scrud.platform.representation.{EditUI, SummaryUI}
import com.github.scrud.{android => _, EntityTypeForTesting => _,_}
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import com.github.scrud.android.action.AndroidOperation._
import scala.Some
import scala.util.Success

/**
 * A behavior specification for Android EditUI and DisplayUI fields.
 * @author Eric Pabst (epabst@gmail.com)
 */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class ViewFieldSpec extends ScrudRobolectricSpecBase {
  class MyEntity(var string: String, var number: Int)
  val itemLayoutId = _root_.android.R.layout.simple_spinner_dropdown_item
  Locale.setDefault(Locale.US)
  private val platformDriver = AndroidPlatformDriverForTesting
  val entityName = EntityTypeForTesting.entityName
  val fieldName = EntityTypeForTesting.name.fieldName

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
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).get().commandContext
    val viewGroup = mock[View]
    val view1 = mockView[EditText]
    stub(viewGroup.findViewById(R.id.edit_name)).toReturn(view1)
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val targetField = viewField.toAdaptableField.findTargetField(EditUI).get
    val copyContext = new CopyContext(UriPath.EMPTY, commandContext)
    targetField.updateValue(viewGroup, None, copyContext)
    commandContext.waitUntilIdle()
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
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).get().commandContext
    val viewGroup = mock[View]
    stub(viewGroup.findViewById(Matchers.anyInt())).toReturn(null)
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val targetField = viewField.toAdaptableField.findTargetField(EditUI)
    val copyContext = new CopyContext(UriPath.EMPTY, commandContext)
    targetField.get.updateValue(viewGroup, None, copyContext)
    commandContext.waitUntilIdle()
  }

  @Test
  def itMustConvertNullToNone() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).create().get()
    val commandContext = activity.commandContext
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val view = new EditText(activity)
    view.setText(null)
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, commandContext)) must be (None)
  }

  @Test
  def itMustConvertEmptyStringToNone() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).create().get()
    val commandContext = activity.commandContext
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val view = new EditText(activity)
    view.setText("")
    viewField.findSourceField(EditUI).get.findValue(view, new CopyContext(UriPath.EMPTY, commandContext)) must be (None)
  }

  @Test
  def itMustTrimStrings() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).create().get()
    val commandContext = activity.commandContext
    val viewField = platformDriver.field(entityName, fieldName, TitleQT, Seq(SummaryUI, EditUI))
    val viewGroup = mock[View]
    val view = new EditText(activity)
    stub(viewGroup.findViewById(R.id.edit_name)).toReturn(view)
    view.setText(" hello world ")
    viewField.findSourceField(EditUI).get.findValue(viewGroup, new CopyContext(UriPath.EMPTY, commandContext)) must be (Some("hello world"))

    view.setText(" ")
    viewField.findSourceField(EditUI).get.findValue(viewGroup, new CopyContext(UriPath.EMPTY, commandContext)) must be (None)
  }

  @Test
  def formattedTextViewMustUseToEditStringConverterForEditTextView() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).get().commandContext
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val viewGroup = mock[View]
    val editView = mockView[EditText]
    stub(viewGroup.findViewById(R.id.edit_name)).toReturn(editView)
    viewField.toAdaptableField.findTargetField(EditUI).get.updateValue(viewGroup, Some("marbles"),
      new CopyContext(UriPath.EMPTY, commandContext))
    commandContext.waitUntilIdle()
    verify(editView).setText("marbles to edit")
  }

  @Test
  def formattedTextViewMustUseToDisplayStringConverterForTextView() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).create().get()
    val commandContext = activity.commandContext
    val viewField = platformDriver.field(entityName, fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val viewGroup = mock[View]
    val textView = new TextView(activity)
    stub(viewGroup.findViewById(R.id.name)).toReturn(textView)
    val copyContext = new CopyContext(UriPath.EMPTY, commandContext)
    val targetField = viewField.toAdaptableField.findTargetField(SummaryUI).get
    targetField.updateValue(viewGroup, Some("marbles"), copyContext)
    commandContext.waitUntilIdle()
    textView.getText.toString must be ("marbles on display")
  }

  @Test
  def formattedTextViewMustTrimAndUseFromStringConverterWhenGetting() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).create().get()
    val commandContext = activity.commandContext
    val viewField = platformDriver.field(entityName, EntityTypeForTesting.name.fieldName, stringConvertibleType, Seq(SummaryUI, EditUI))
    val viewGroup = mock[View]
    val editText = new EditText(activity)
    stub(viewGroup.findViewById(R.id.edit_name)).toReturn(editText)
    editText.setText("  given text   ")
    viewField.toAdaptableField.findSourceField(EditUI).get.findValue(viewGroup, new CopyContext(UriPath.EMPTY, commandContext)) must be (Some("parsed given text"))
  }
}
