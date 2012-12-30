package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.res.R
import android.content.Intent
import java.io.File
import android.os.Environment
import android.provider.MediaStore
import com.github.triangle._
import android.graphics.BitmapFactory
import android.graphics.drawable.{BitmapDrawable, Drawable}
import com.github.scrud.android.action._
import com.github.scrud.state.ApplicationVar
import com.github.scrud.{CrudContext, CrudContextField}
import com.github.scrud.util.{Common, CachedFunction}

/** A ViewField for an image that can be captured using the camera.
  * It currently puts the image into external storage, which requires the following in the AndroidManifest.xml:
  * {{{<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />}}}
  * @author Eric Pabst (epabst@gmail.com)
  */
object CapturedImageView extends ViewField[Uri](new FieldLayout {
  val displayXml = <ImageView android:adjustViewBounds="true"/>
  val editXml = <ImageView android:adjustViewBounds="true" android:clickable="true"/>
}) {
  private object DrawableByUriCache extends ApplicationVar[CachedFunction[Uri,Drawable]]

  private def bitmapFactoryOptions(inSampleSize: Int) = {
    val options = new BitmapFactory.Options
    options.inDither = true
    options.inSampleSize = inSampleSize
    options
  }

  private def setImageUri(imageView: ImageView, uriOpt: Option[Uri], crudContext: CrudContext) {
    imageView.setImageBitmap(null)
    uriOpt match {
      case Some(uri) =>
        imageView.setTag(uri.toString)
        val contentResolver = imageView.getContext.getContentResolver
        val cachingResolver = DrawableByUriCache.getOrSet(crudContext, CachedFunction(uri => {
          val displayMetrics = imageView.getContext.getResources.getDisplayMetrics
          val maxHeight: Int = displayMetrics.heightPixels
          val maxWidth: Int = displayMetrics.widthPixels
          val optionsToDecodeBounds = new BitmapFactory.Options()
          optionsToDecodeBounds.inJustDecodeBounds = true
          Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
            BitmapFactory.decodeStream(stream, null, optionsToDecodeBounds)
          }
          val ratio = math.min(optionsToDecodeBounds.outHeight / maxHeight, optionsToDecodeBounds.outWidth / maxWidth)
          val inSampleSize = math.max(Integer.highestOneBit(ratio), 1)
          Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
            new BitmapDrawable(BitmapFactory.decodeStream(stream, null, bitmapFactoryOptions(inSampleSize)))
          }
        }))
        imageView.setImageDrawable(cachingResolver(uri))
      case None =>
        imageView.setImageResource(R.drawable.android_camera_256)
    }
  }

  private def tagToUri(tag: Object): Option[Uri] = Option(tag.asInstanceOf[String]).map(Uri.parse(_))

  private def imageUri(imageView: ImageView): Option[Uri] = tagToUri(imageView.getTag)

  // This could be any value.  Android requires that it is some entry in R.
  val DefaultValueTagKey = R.drawable.icon

  lazy val dcimDirectory: File = {
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    dir.mkdirs()
    dir
  }

  protected val delegate = Getter[Uri] {
    case OperationResponseExtractor(Some(response)) && ViewExtractor(Some(view)) =>
      Option(response.intent).map(_.getData).orElse(tagToUri(view.getTag(DefaultValueTagKey)))
  } + Getter((v: ImageView) => imageUri(v)) + Setter[Uri] {
    case UpdaterInput(ViewExtractor(Some(view: ImageView)), uriOpt, CrudContextField(Some(crudContext))) =>
      setImageUri(view, uriOpt, crudContext)
  } + OnClickOperationSetter(view => StartActivityForResultOperation(view, {
    val intent = new Intent("android.media.action.IMAGE_CAPTURE")
    val imageUri = Uri.fromFile(File.createTempFile("image", ".jpg", dcimDirectory))
    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
    view.setTag(DefaultValueTagKey, imageUri.toString)
    intent
  }))
}
