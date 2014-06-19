package com.github.scrud.android.util

import android.net.Uri
import android.graphics.drawable.Drawable
import scala.util.Try

/**
 * Loads Images and caches, including putting them into an ImageView.
 *@author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:21 PM
 */
class ImageLoader {
  // The sequence of 1, 2, 4, 8, 16, ...
  private lazy val powersOfTwo = Stream.from(0).map(1 << _)

  def loadDrawable(uri: Uri, imageDisplayWidth: Int, imageDisplayHeight: Int, contentResolver: RichContentResolver): Drawable = {
    val optionsToDecodeBounds = contentResolver.decodeBounds(uri)
    val imageHeight: Int = optionsToDecodeBounds.outHeight
    val imageWidth: Int = optionsToDecodeBounds.outWidth
    val firstInSampleSize: Int = calculateSampleSize(imageWidth, imageHeight, imageDisplayWidth, imageDisplayHeight)
    // No use in doing less than 8 pixels of detail.
    val multiplierSeq = powersOfTwo.takeWhile(_ <= imageDisplayWidth / 8)
    val results: Seq[Try[Drawable]] = multiplierSeq.view.map { multiplier =>
      val inSampleSize = firstInSampleSize * multiplier
      Try(contentResolver.decodeBitmap(uri, inSampleSize))
    }
    results.find(_.isSuccess).fold(results.head.get)(_.get)
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
}
