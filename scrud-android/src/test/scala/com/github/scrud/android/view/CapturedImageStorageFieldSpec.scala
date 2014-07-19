package com.github.scrud.android.view

import org.junit.runner.RunWith
import org.junit.Test
import org.mockito.Mockito._
import com.github.scrud.android.action.{ActivityResultDataField, ActivityResult}
import android.net.Uri
import android.content.Intent
import com.github.scrud.android._
import com.github.scrud.copy.{ExtensibleAdaptableField, CopyContext}
import com.github.scrud.UriPath
import com.github.scrud.types.ImageQT
import com.github.scrud.platform.representation.{EditUI, SelectUI}
import com.netaporter
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import com.github.scrud.android.action.AndroidOperation._
import scala.Some

/**
 * A behavior specification for [[com.github.scrud.android.view.CapturedImageStorageField]].
 * @author Eric Pabst (epabst@gmail.com)
 */

@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class CapturedImageStorageFieldSpec extends ScrudRobolectricSpecBase {
  val platformDriver = AndroidPlatformDriverForTesting

  @Test
  def capturedImageViewMustGetImageUriFromOperationResponse() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).get().commandContext
    val field = ActivityResultDataField

    val uri = Uri.parse("file://foo/bar.jpg")
    val intent = mock[Intent]
    stub(intent.getData).toReturn(uri)

    val copyContext: CopyContext = new CopyContext(UriPath.EMPTY, commandContext)
    field.findFieldValue(intent, copyContext) must be (Some(uri))
  }

  @Test
  def capturedImageViewMustGetImageUriFromOperationResponseEvenIfImageIsAlreadySet() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(UpdateActionName)).get().commandContext
    val uri = Uri.parse("file://foo/bar.jpg")
    val intent = mock[Intent]
    stub(intent.getData).toReturn(uri)

    val entityType = new EntityTypeForTesting {
      val imageField = field("image", ImageQT, Seq(EditUI, SelectUI))
    }
    val copyContext: CopyContext = new CopyContext(UriPath.EMPTY, commandContext)

    val viewRef = platformDriver.toViewRef("edit_", entityType.imageField.fieldName)
    val adaptableField: ExtensibleAdaptableField[netaporter.uri.Uri] = entityType.imageField.toAdaptableField
    adaptableField.findSourceField(ActivityResult(viewRef)).get.findValue(intent, copyContext) must be (Some(uri))
  }
}
