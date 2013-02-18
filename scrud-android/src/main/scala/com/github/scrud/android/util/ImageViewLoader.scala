package com.github.scrud.android.util

import android.widget.ImageView
import android.net.Uri
import com.github.scrud.state.{LazyStateVal, State}
import ref.WeakReference
import android.content.Context
import android.graphics.drawable.Drawable
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.asScalaConcurrentMap

/**
 * Loads Images into ImageViews.
 *@author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:21 PM
 */
class ImageViewLoader(imageLoader: ImageLoader = new ImageLoader) {
  def getDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context, stateForCaching: State): Drawable = {
    val cache = DrawableByUriCache.get(stateForCaching)
    cache.get(uri) match {
      case Some(weakReference) =>
        weakReference.get.getOrElse {
          val drawable = imageLoader.loadDrawable(uri, displayWidth, displayHeight, context)
          cache.put(uri, new WeakReference(drawable))
          drawable
        }
      case None =>
        val drawable = imageLoader.loadDrawable(uri, displayWidth, displayHeight, context)
        cache.put(uri, new WeakReference(drawable))
        drawable
    }
  }

  def setImageDrawable(imageView: ImageView, uriOpt: Option[Uri], stateForCaching: State) {
    val context = imageView.getContext
    displayDefault(imageView)
    uriOpt.foreach { uri =>
      val uriString = uri.toString
      imageView.setTag(uriString)
      val displayMetrics = context.getResources.getDisplayMetrics
      val displayWidth: Int = displayMetrics.widthPixels
      val displayHeight: Int = displayMetrics.heightPixels
      val drawable = getDrawable(uri, displayWidth, displayHeight, context, stateForCaching)
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
