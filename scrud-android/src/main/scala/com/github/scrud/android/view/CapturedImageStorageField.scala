package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import java.io.File
import android.os.Environment
import com.github.scrud.android.util.ImageViewLoader
import com.github.scrud.android.{AndroidPlatformDriver, AndroidCommandContext}
import com.github.scrud.copy.CopyContext
import android.R
import com.github.scrud.util.Name
import com.github.scrud.platform.PlatformTypes.ImgKey
import com.github.scrud.android.action.StartActivityForResultOperation
import android.view.View
import android.content.Intent
import android.provider.MediaStore
import android.view.View.OnClickListener
import com.github.scrud.UriPath
import com.github.scrud.android.view.CapturedImageStorageField.TriggerImageCapture

/** A ViewStorageField for an image that can be captured using the camera.
  * It currently puts the image into external storage, which requires the following in the AndroidManifest.xml:
  * {{{<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}}}
  * @author Eric Pabst (epabst@gmail.com)
  */
class CapturedImageStorageField(platformDriver: AndroidPlatformDriver)
    extends TypedViewStorageField[ImageView,Uri](<ImageView android:adjustViewBounds="true" android:clickable="true"/>) {
  private val androidCamera256: ImgKey = platformDriver.tryImageKey(Name("android_camera_256")).get

  val imageViewLoader = new ImageViewLoader() {
    override protected def displayDefault(imageView: ImageView) {
      imageView.setImageResource(androidCamera256)
    }
  }

  /** Get some value or None from the given source. */
  override def findFieldValue(imageView: ImageView, context: AndroidCommandContext): Option[Uri] = {
    Option(imageView.getTag.asInstanceOf[String]).map(Uri.parse(_))
  }

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(imageView: ImageView, uriOpt: Option[Uri], commandContext: AndroidCommandContext, context: CopyContext): ImageView = {
    imageViewLoader.setImageDrawable(imageView, uriOpt, commandContext.applicationState)
    if (!imageView.isClickable) {
      imageView.setClickable(true)
    }
    val sourceUri = context.sourceUri
    if (imageView.isClickable && !imageView.hasOnClickListeners) {
      imageView.setOnClickListener(new TriggerImageCapture(sourceUri, commandContext))
    }
    imageView
  }
}

object CapturedImageStorageField {
  lazy val dcimDirectory: File = {
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    dir.mkdirs()
    dir
  }

  // This could be any value.  Android requires that it is some entry in R.
  private[view] val DefaultValueTagKey = R.drawable.btn_plus

  private class TriggerImageCapture(sourceUri: UriPath, commandContext: AndroidCommandContext) extends OnClickListener {
    override def onClick(view: View) {
      commandContext.withExceptionReporting {
        StartActivityForResultOperation(view, {
          val intent = new Intent("android.media.action.IMAGE_CAPTURE")
          val imageUri = Uri.fromFile(File.createTempFile("image", ".jpg", CapturedImageStorageField.dcimDirectory))
          intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
          view.setTag(CapturedImageStorageField.DefaultValueTagKey, imageUri.toString)
          intent
        }).invoke(sourceUri, commandContext)
      }
    }
  }
}
