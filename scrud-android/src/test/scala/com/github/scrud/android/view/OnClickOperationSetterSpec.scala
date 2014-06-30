package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.junit.Test
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import android.view.View
import com.github.scrud.android.action.AndroidOperation
import com.github.scrud.android.{EntityTypeForTesting, AndroidCommandContextForTesting, CustomRobolectricTestRunner}
import com.github.scrud.UriPath
import com.github.scrud.copy.CopyContext
import org.robolectric.annotation.Config

/** A specification of [[com.github.scrud.android.view.OnClickSetterField]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class OnClickOperationSetterSpec extends MockitoSugar {
  @Test
  def itMustSetOnClickListenerWhenClicableIsTrue() {
    val operation = mock[AndroidOperation]
    val view = mock[View]
    stub(view.isClickable).toReturn(true)
    val targetField = OnClickSetterField(_ => operation)
    targetField.updateFieldValue(view, None, makeCopyContext())
    verify(view).setOnClickListener(any())
  }

  @Test
  def itMustNotSetOnClickListenerWhenClickableIsFalse() {
    val operation = mock[AndroidOperation]
    val view = mock[View]
    stub(view.isClickable).toReturn(false)
    val targetField = OnClickSetterField(_ => operation)
    targetField.updateFieldValue(view, None, makeCopyContext())
    verify(view, never()).setOnClickListener(any())
  }

  private def makeCopyContext(): CopyContext =
    new CopyContext(UriPath.EMPTY, new AndroidCommandContextForTesting(EntityTypeForTesting))
}
