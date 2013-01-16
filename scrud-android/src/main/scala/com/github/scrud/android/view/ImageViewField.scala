package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.res.R
import com.github.triangle._
import android.graphics.BitmapFactory
import android.graphics.drawable.{BitmapDrawable, Drawable}
import com.github.scrud.state.ApplicationVar
import com.github.scrud.{CrudContext, CrudContextField}
import com.github.scrud.util.{Common, CachedFunction}
import xml.NodeSeq

object ImageViewField extends ImageViewField(new FieldLayout {
  val displayXml = <ImageView android:adjustViewBounds="true"/>
  val editXml = NodeSeq.Empty
})

/** A ViewField for an image to be displayed.
  * @author Eric Pabst (epabst@gmail.com)
  */
class ImageViewField(fieldLayout: FieldLayout) extends ViewField[Uri](fieldLayout) {
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
        displayDefault(imageView)
    }
  }

  /** This can be overridden to show something if desired. */
  protected def displayDefault(imageView: ImageView) {
    // Clear the ImageView by default
    imageView.setImageURI(null)
  }

  protected def tagToUri(tag: Object): Option[Uri] = Option(tag.asInstanceOf[String]).map(Uri.parse(_))

  private def imageUri(imageView: ImageView): Option[Uri] = tagToUri(imageView.getTag)

  // This could be any value.  Android requires that it is some entry in R.
  val DefaultValueTagKey = R.drawable.icon

  protected def createDelegate: PortableField[Uri] = Getter((v: ImageView) => imageUri(v)) + Setter[Uri] {
    case UpdaterInput(ViewExtractor(Some(view: ImageView)), uriOpt, CrudContextField(Some(crudContext))) =>
      setImageUri(view, uriOpt, crudContext)
  }

  /** To override, override createDelegate instead. This is because super is not available for a val. */
  protected final val delegate = createDelegate
}

private object DrawableByUriCache extends ApplicationVar[CachedFunction[Uri,Drawable]]
