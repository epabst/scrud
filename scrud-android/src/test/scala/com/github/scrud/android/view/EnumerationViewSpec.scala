package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import org.scalatest.mock.MockitoSugar
import android.widget._
import java.util.Locale
import com.github.scrud.android._
import com.github.scrud.copy.CopyContext
import com.github.scrud.UriPath
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import scala.Some
import android.content.Intent

/** A behavior specification for [[com.github.scrud.android.view.EnumerationView]].
  * @author Eric Pabst (epabst@gmail.com)
  */

@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class EnumerationViewSpec extends MustMatchers with MockitoSugar {
  class MyEntity(var string: String, var number: Int)
  val itemLayoutId = android.R.layout.simple_spinner_dropdown_item
  Locale.setDefault(Locale.US)
  object MyEnum extends Enumeration {
    val A = Value("a")
    val B = Value("b")
    val C = Value("c")
  }
  val enumerationView = EnumerationView[MyEnum.Value](MyEnum)

  def makeContext() = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
    withIntent(new Intent(Intent.ACTION_MAIN)).create().get()

  private def makeCopyContext(): CopyContext = 
    new CopyContext(UriPath.EMPTY, new AndroidCommandContextForTesting(EntityTypeForTesting))
  
  @Test
  def itMustSetTheAdapterForAnAdapterView() {
    val adapterView: AdapterView[BaseAdapter] = new Spinner(makeContext()).asInstanceOf[AdapterView[BaseAdapter]]
    enumerationView.updateFieldValue(adapterView, Some(MyEnum.C), makeCopyContext())
    val adapter = adapterView.getAdapter
    (0 to (adapter.getCount - 1)).toList.map(adapter.getItem) must be (List(MyEnum.A, MyEnum.B, MyEnum.C))
  }

  @Test
  def itMustSetTheAdapterForAnAdapterViewEvenIfTheValueIsNotSet() {
    val adapterView: AdapterView[BaseAdapter] = new Spinner(makeContext()).asInstanceOf[AdapterView[BaseAdapter]]
    enumerationView.updateFieldValue(adapterView, None, makeCopyContext())
    val adapter = adapterView.getAdapter
    (0 to (adapter.getCount - 1)).toList.map(adapter.getItem) must be (List(MyEnum.A, MyEnum.B, MyEnum.C))
  }

  @Test
  def itMustSetThePositionCorrectly() {
    val adapterView: AdapterView[BaseAdapter] = new Spinner(makeContext()).asInstanceOf[AdapterView[BaseAdapter]]
    enumerationView.updateFieldValue(adapterView, Some(MyEnum.C), makeCopyContext())
    adapterView.getSelectedItemPosition must be (2)
  }

  @Test
  def itMustHandleInvalidValueForAnAdapterView() {
    val field = enumerationView
    val adapterView: AdapterView[BaseAdapter] = new Spinner(makeContext()).asInstanceOf[AdapterView[BaseAdapter]]
    field.updateFieldValue(adapterView, None, makeCopyContext())
    adapterView.getSelectedItemPosition must be (AdapterView.INVALID_POSITION)
  }
}
