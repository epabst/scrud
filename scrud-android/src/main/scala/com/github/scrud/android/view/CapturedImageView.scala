package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.res.R
import android.content.Intent
import java.io.File
import android.os.Environment
import android.provider.MediaStore
import com.github.triangle._
import com.github.scrud.android.action._

/** A ViewField for an image that can be captured using the camera.
  * It currently puts the image into external storage, which requires the following in the AndroidManifest.xml:
  * {{{<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}}}
  * @author Eric Pabst (epabst@gmail.com)
  */
object CapturedImageView extends ImageViewField(new FieldLayout {
  val displayXml = <ImageView android:adjustViewBounds="true"/>
  val editXml = <ImageView android:adjustViewBounds="true" android:clickable="true"/>
}) {
  lazy val dcimDirectory: File = {
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    dir.mkdirs()
    dir
  }

  override protected def createDelegate = Getter[Uri] {
      case OperationResponseExtractor(Some(response)) && ViewExtractor(Some(view)) =>
        Option(response.intent).map(_.getData).orElse(tagToUri(view.getTag(DefaultValueTagKey)))
    } + super.createDelegate + OnClickOperationSetter(view => StartActivityForResultOperation(view, {
    val intent = new Intent("android.media.action.IMAGE_CAPTURE")
    val imageUri = Uri.fromFile(File.createTempFile("image", ".jpg", dcimDirectory))
    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
    view.setTag(DefaultValueTagKey, imageUri.toString)
    intent
  }))

  override protected def displayDefault(imageView: ImageView) {
    imageView.setImageResource(R.drawable.android_camera_256)
  }
}
