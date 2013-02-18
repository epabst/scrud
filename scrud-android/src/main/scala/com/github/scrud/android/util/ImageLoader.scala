package com.github.scrud.android.util

import android.net.Uri
import android.content.Context
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.BitmapFactory
import com.github.scrud.util.Common

/**
 * Loads Images and caches, including putting them into an ImageView.
 *@author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:21 PM
 */
class ImageLoader {
  def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context): Drawable = {
    val optionsToDecodeBounds = new BitmapFactory.Options()
    optionsToDecodeBounds.inJustDecodeBounds = true
    val contentResolver = context.getContentResolver
    Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
      BitmapFactory.decodeStream(stream, null, optionsToDecodeBounds)
    }
    val imageHeight: Int = optionsToDecodeBounds.outHeight
    val imageWidth: Int = optionsToDecodeBounds.outWidth
    val firstInSampleSize: Int = calculateSampleSize(imageWidth, imageHeight, displayWidth, displayHeight)
    val results: Seq[Either[Drawable, Throwable]] = Stream.range(firstInSampleSize, imageHeight, 2).view.map { inSampleSize =>
      Common.evaluateOrIntercept {
        Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
          new BitmapDrawable(BitmapFactory.decodeStream(stream, null, bitmapFactoryOptions(inSampleSize)))
        }
      }
    }
    results.find(_.isLeft).map(_.left.get).getOrElse(throw results.head.right.get)
  }

  def calculateSampleSize(imageWidth: Int, imageHeight: Int, displayWidth: Int, displayHeight: Int): Int = {
    // Use max instead of min because the image's aspect ratio will probably be preserved, which means that
    // for a picture that is really tall and narrow or that is really short and wide, the dimension that limits
    // the displayed size of the picture should dictate how much detail is decoded.
    val ratio = math.max(imageHeight / displayHeight, imageWidth / displayWidth)
    // Use highestOneBit so that the sample size is a power of 2, which makes it more efficient to do the sampling.
    // If ratio is already a power of 2, it is used unchanged.
    math.max(Integer.highestOneBit(ratio), 1)
  }

  private def bitmapFactoryOptions(inSampleSize: Int) = {
    val options = new BitmapFactory.Options
    options.inDither = true
    options.inSampleSize = inSampleSize
    options
  }
}
