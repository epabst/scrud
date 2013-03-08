package com.github.scrud.android.util

import android.widget.ImageView
import android.net.Uri
import com.github.scrud.state.{LazyStateVal, State}
import ref.WeakReference
import android.graphics.drawable.Drawable
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.asScalaConcurrentMap
import com.github.scrud.android.util.ViewUtil.withViewOnUIThread

/**
 * Loads Images into ImageViews.
 *@author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:21 PM
 */
class ImageViewLoader(imageLoader: ImageLoader = new ImageLoader) {
  def getDrawable(uri: Uri, imageDisplayWidth: Int, imageDisplayHeight: Int, contentResolver: RichContentResolver, stateForCaching: State): Drawable = {
    val cache = DrawableByUriCache.get(stateForCaching)
    cache.get(uri) match {
      case Some(weakReference) =>
        weakReference.get.getOrElse {
          val drawable = imageLoader.loadDrawable(uri, imageDisplayWidth, imageDisplayHeight, contentResolver)
          cache.put(uri, new WeakReference(drawable))
          drawable
        }
      case None =>
        val drawable = imageLoader.loadDrawable(uri, imageDisplayWidth, imageDisplayHeight, contentResolver)
        cache.put(uri, new WeakReference(drawable))
        drawable
    }
  }

  def setImageDrawable(imageView: ImageView, uriOpt: Option[Uri], stateForCaching: State) {
    val context = imageView.getContext
    displayDefault(imageView)
    uriOpt.foreach { uri =>
      val uriString = uri.toString
      withViewOnUIThread(imageView) {
        _.setTag(uriString)
      }
      val imageViewWidth = imageView.getWidth
      val imageViewHeight = imageView.getHeight
      val imageViewSizeIsProvided = imageViewWidth > 0 && imageViewHeight > 0
      val displayMetricsToUseOpt = if (imageViewSizeIsProvided) None else Some(context.getResources.getDisplayMetrics)
      val imageDisplayWidth: Int = displayMetricsToUseOpt.map(_.widthPixels).getOrElse(imageViewWidth)
      val imageDisplayHeight: Int = displayMetricsToUseOpt.map(_.heightPixels).getOrElse(imageViewHeight)
      val contentResolver = new RichContentResolver(context)
      val drawable = getDrawable(uri, imageDisplayWidth, imageDisplayHeight, contentResolver, stateForCaching)
      withViewOnUIThread(imageView) {
        _.setImageDrawable(drawable)
      }
    }
  }

  /** This can be overridden to show something if desired. */
  protected def displayDefault(imageView: ImageView) {
    // Clear the ImageView by default
    withViewOnUIThread(imageView) {
      _.setImageURI(null)
    }
  }
}

// The WeakReference must directly contain the Drawable or else it might be released due to no references
// existing to the intermediate container.
private object DrawableByUriCache extends LazyStateVal[mutable.ConcurrentMap[Uri,WeakReference[Drawable]]](
  new ConcurrentHashMap[Uri,WeakReference[Drawable]]()
)
