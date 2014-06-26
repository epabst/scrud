package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.github.scrud.android.action.{ActivityResultDataField, ActivityResult}
import android.net.Uri
import android.content.Intent
import com.github.scrud.android.{AndroidPlatformDriver, CustomRobolectricTestRunner}
import com.github.scrud.copy.{ExtensibleAdaptableField, CopyContext}
import com.github.scrud.{EntityTypeForTesting, UriPath}
import com.github.scrud.context.CommandContextForTesting
import com.github.scrud.types.ImageQT
import com.github.scrud.platform.representation.{EditUI, SelectUI}
import scala.Some
import com.github.scrud.android.testres.R
import com.netaporter

/**
 * A behavior specification for [[com.github.scrud.android.view.CapturedImageStorageField]].
 * @author Eric Pabst (epabst@gmail.com)
 */

@RunWith(classOf[CustomRobolectricTestRunner])
class CapturedImageStorageFieldSpec extends MustMatchers with MockitoSugar {
  val platformDriver = new AndroidPlatformDriver(classOf[R])

  @Test
  def capturedImageViewMustGetImageUriFromOperationResponse() {
    val field = ActivityResultDataField

    val uri = Uri.parse("file://foo/bar.jpg")
    val intent = mock[Intent]
    stub(intent.getData).toReturn(uri)

    val copyContext: CopyContext = new CopyContext(UriPath.EMPTY, new CommandContextForTesting(EntityTypeForTesting))
    field.findFieldValue(intent, copyContext) must be (Some(uri))
  }

  @Test
  def capturedImageViewMustGetImageUriFromOperationResponseEvenIfImageIsAlreadySet() {
    val uri = Uri.parse("file://foo/bar.jpg")
    val intent = mock[Intent]
    stub(intent.getData).toReturn(uri)

    val entityType = new EntityTypeForTesting {
      val imageField = field("image", ImageQT, Seq(EditUI, SelectUI))
    }
    val copyContext: CopyContext = new CopyContext(UriPath.EMPTY, new CommandContextForTesting(EntityTypeForTesting))

    val viewRef = platformDriver.toViewRef("", entityType.imageField.fieldName)
    val adaptableField: ExtensibleAdaptableField[netaporter.uri.Uri] = entityType.imageField.toAdaptableField
    adaptableField.findSourceField(ActivityResult(viewRef)).get.findValue(intent, copyContext) must be (Some(uri))
  }
}
