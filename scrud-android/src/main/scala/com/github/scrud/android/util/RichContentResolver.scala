package com.github.scrud.android.util

import android.content.{Context, ContentResolver}
import android.net.Uri
import android.graphics.BitmapFactory
import com.github.scrud.util.Common
import android.graphics.drawable.BitmapDrawable

/**
 * A wrapper for a [[android.content.ContentResolver]] for convenience.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/18/13
 * Time: 11:50 AM
 */
class RichContentResolver(contentResolver: ContentResolver) {
  def this(context: Context) { this(context.getContentResolver) }

  def decodeBounds(uri: Uri): BitmapFactory.Options = {
    val optionsToDecodeBounds = new BitmapFactory.Options()
    optionsToDecodeBounds.inJustDecodeBounds = true
    Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
      BitmapFactory.decodeStream(stream, null, optionsToDecodeBounds)
    }
    optionsToDecodeBounds
  }

  def decodeBitmap(uri: Uri, inSampleSize: Int): BitmapDrawable = {
    Common.withCloseable(contentResolver.openInputStream(uri)) { stream =>
      new BitmapDrawable(BitmapFactory.decodeStream(stream, null, bitmapFactoryOptions(inSampleSize)))
    }
  }

  private def bitmapFactoryOptions(inSampleSize: Int) = {
    val options = new BitmapFactory.Options
    options.inDither = true
    options.inSampleSize = inSampleSize
    options
  }
}
