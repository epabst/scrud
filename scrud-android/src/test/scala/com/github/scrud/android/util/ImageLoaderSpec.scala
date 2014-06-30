package com.github.scrud.android.util

import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.junit.runner.RunWith
import com.github.scrud.android.CustomRobolectricTestRunner
import org.junit.Test
import android.net.Uri
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import org.robolectric.annotation.Config

/**
 * A behavior specification for [[com.github.scrud.android.util.ImageLoader]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/18/13
 * Time: 12:12 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class ImageLoaderSpec extends MustMatchers with MockitoSugar {
  @Test
  def loadDrawable_shouldRetryUsingPowersOf2WhileExceedsAvailableMemory() {
    val uri = mock[Uri]
    val contentResolver = mock[RichContentResolver]
    val mockDrawable = mock[BitmapDrawable]
    val options = new BitmapFactory.Options()
    options.outWidth = 50000
    options.outHeight = 40000
    when(contentResolver.decodeBounds(uri)).thenReturn(options)
    when(contentResolver.decodeBitmap(uri, 64)).thenThrow(new OutOfMemoryError("testing"))
    when(contentResolver.decodeBitmap(uri, 128)).thenThrow(new OutOfMemoryError("testing"))
    when(contentResolver.decodeBitmap(uri, 256)).thenThrow(new OutOfMemoryError("testing"))
    when(contentResolver.decodeBitmap(uri, 512)).thenThrow(new OutOfMemoryError("testing"))
    when(contentResolver.decodeBitmap(uri, 1024)).thenReturn(mockDrawable)

    val imageLoader = new ImageLoader()
    val drawable = imageLoader.loadDrawable(uri, 500, 400, contentResolver)
    drawable must be (mockDrawable)
    verify(contentResolver).decodeBitmap(uri, 64)
    verify(contentResolver).decodeBitmap(uri, 128)
    verify(contentResolver).decodeBitmap(uri, 256)
    verify(contentResolver).decodeBitmap(uri, 512)
    verify(contentResolver).decodeBitmap(uri, 1024)
  }
}
