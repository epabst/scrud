package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.junit.Test
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import android.view.View
import com.github.scrud.android.action.{ActivityWithState, AndroidOperation}
import com.github.scrud.CrudApplication
import com.github.scrud.android.{CustomRobolectricTestRunner, AndroidCrudContext}
import com.github.triangle.{GetterInput, PortableField}
import com.github.scrud.UriPath

/** A specification of [[com.github.scrud.android.view.OnClickOperationSetter]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class OnClickOperationSetterSpec extends MockitoSugar {
  @Test
  def itMustSetOnClickListenerWhenClicableIsTrue() {
    val operation = mock[AndroidOperation]
    val view = mock[View]
    stub(view.isClickable).toReturn(true)
    val setter = OnClickOperationSetter[Unit](_ => operation)
    val crudContext = new AndroidCrudContext(mock[MyActivityWithState], mock[CrudApplication])
    setter.updateWithValue(view, None, GetterInput(UriPath.EMPTY, crudContext, PortableField.UseDefaults))
    verify(view).setOnClickListener(any())
  }

  @Test
  def itMustNotSetOnClickListenerWhenClickableIsFalse() {
    val operation = mock[AndroidOperation]
    val view = mock[View]
    stub(view.isClickable).toReturn(false)
    val setter = OnClickOperationSetter[Unit](_ => operation)
    val crudContext = new AndroidCrudContext(mock[MyActivityWithState], mock[CrudApplication])
    setter.updateWithValue(view, None, GetterInput(UriPath.EMPTY, crudContext, PortableField.UseDefaults))
    verify(view, never()).setOnClickListener(any())
  }
}

class MyActivityWithState extends ActivityWithState
