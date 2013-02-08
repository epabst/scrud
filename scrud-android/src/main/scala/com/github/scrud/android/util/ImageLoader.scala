package com.github.scrud.android.util

import android.widget.ImageView
import android.net.Uri
import com.github.scrud.state.{LazyStateVal, State}
import ref.WeakReference
import android.content.Context
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.BitmapFactory
import com.github.scrud.util.Common
import collection.mutable
import scala.collection.JavaConversions._
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads Images and caches, including putting them into an ImageView.
 *@author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:21 PM
 */
class ImageLoader {
  def getDrawable(uri: Uri, context: Context, stateForCaching: State): Drawable = {
    val cache = DrawableByUriCache.get(stateForCaching)
    cache.get(uri) match {
      case Some(weakReference) =>
        weakReference.get.getOrElse {
          val drawable = loadDrawable(uri, context)
          cache.put(uri, new WeakReference(drawable))
          drawable
        }
      case None =>
        val drawable = loadDrawable(uri, context)
        cache.put(uri, new WeakReference(drawable))
        drawable
    }
  }

  def loadDrawable(uri: Uri, context: Context): Drawable = {
    val displayMetrics = context.getResources.getDisplayMetrics
    val maxHeight: Int = displayMetrics.heightPixels
    val maxWidth: Int = displayMetrics.widthPixels
    val optionsToDecodeBounds = new BitmapFactory.Options()
    optionsToDecodeBounds.inJustDecodeBounds = true
    val contentResolver = context.getContentResolver
    Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
      BitmapFactory.decodeStream(stream, null, optionsToDecodeBounds)
    }
    // Use max instead of min because the image's aspect ratio will probably be preserved, which means that
    // for a picture that is really tall and narrow or that is really short and wide, the dimension that limits
    // the displayed size of the picture should dictate how much detail is decoded.
    val ratio = math.max(optionsToDecodeBounds.outHeight / maxHeight, optionsToDecodeBounds.outWidth / maxWidth)
    val firstInSampleSize = math.max(Integer.highestOneBit(ratio), 1)
    val results: Seq[Either[Drawable,Throwable]] = Stream.range(firstInSampleSize, optionsToDecodeBounds.outHeight, 2).view.map { inSampleSize =>
      Common.evaluateOrIntercept {
        Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
          new BitmapDrawable(BitmapFactory.decodeStream(stream, null, bitmapFactoryOptions(inSampleSize)))
        }
      }
    }
    results.find(_.isLeft).map(_.left.get).getOrElse(throw results.head.right.get)
  }

  private def bitmapFactoryOptions(inSampleSize: Int) = {
    val options = new BitmapFactory.Options
    options.inDither = true
    options.inSampleSize = inSampleSize
    options
  }

  def setImageDrawable(imageView: ImageView, uriOpt: Option[Uri], stateForCaching: State) {
    val context = imageView.getContext
    displayDefault(imageView)
    uriOpt.foreach { uri =>
      val uriString = uri.toString
      imageView.setTag(uriString)
      val drawable = getDrawable(uri, context, stateForCaching)
      imageView.setImageDrawable(drawable)
    }
  }

  /** This can be overridden to show something if desired. */
  protected def displayDefault(imageView: ImageView) {
    // Clear the ImageView by default
    imageView.setImageURI(null)
  }
}

// The WeakReference must directly contain the Drawable or else it might be released due to no references
// existing to the intermediate container.
private object DrawableByUriCache extends LazyStateVal[mutable.ConcurrentMap[Uri,WeakReference[Drawable]]](
  new ConcurrentHashMap[Uri,WeakReference[Drawable]]()
)
