package com.github.scrud.android.util

import org.junit.runner.RunWith
import com.github.scrud.android.CustomRobolectricTestRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.junit.Test
import com.github.scrud.state.State
import android.net.Uri
import android.content.Context
import collection.mutable
import android.graphics.drawable.Drawable

/**
 * Behavior specification for [[com.github.scrud.android.util.ImageLoader]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:38 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class ImageViewLoaderSpec extends MustMatchers with MockitoSugar {
  @Test
  def getImage_shouldDistinguishByUri() {
    val loadedDrawables = mutable.Buffer.empty[Drawable]
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context) = {
        val drawable = mock[Drawable]
        loadedDrawables += drawable
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State() {}
    val uri1 = mock[Uri]
    val uri2 = mock[Uri]
    val drawable1 = imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    drawable1 must be(loadedDrawables.head)

    imageViewLoader.getDrawable(uri2, 100, 100, null, state) must not be (drawable1)
    loadedDrawables.size must be (2)
  }

  @Test
  def getImage_shouldCache() {
    val loadedDrawables = mutable.Buffer.empty[Drawable]
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context) = {
        val drawable = mock[Drawable]
        loadedDrawables += drawable
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State() {}
    val uri1 = mock[Uri]
    val drawable = imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    drawable must be(loadedDrawables.head)

    val result2 = imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    loadedDrawables.size must be (1)
    result2 must be (drawable)
  }

  @Test
  def getImage_cacheShouldNotCauseOutOfMemoryError() {
    var loadCount = 0
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State() {}
    val uri1 = mock[Uri]
    imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    loadCount must be (1)

    System.gc()
    imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    loadCount must be (2)
  }

  @Test
  def getImage_cacheShouldOnlyReleaseIfNoOtherReferencesToImage() {
    var loadCount = 0
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, context: Context) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State() {}
    val uri1 = mock[Uri]
    val drawable1 = imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    loadCount must be (1)

    System.gc()
    imageViewLoader.getDrawable(uri1, 100, 100, null, state) must be (drawable1)
    loadCount must be (1)
  }
}
