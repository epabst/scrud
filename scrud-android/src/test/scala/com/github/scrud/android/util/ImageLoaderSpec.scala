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
class ImageLoaderSpec extends MustMatchers with MockitoSugar {
  @Test
  def getImage_shouldDistinguishByUri() {
    val loadedDrawable = mutable.Buffer.empty[Drawable]
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, context: Context) = {
        val drawable = mock[Drawable]
        loadedDrawable += drawable
        drawable
      }
    }

    val state = new State() {}
    val uri1 = mock[Uri]
    val uri2 = mock[Uri]
    val drawable1 = imageLoader.getDrawable(uri1, null, state)
    drawable1 must be(loadedDrawable.head)

    imageLoader.getDrawable(uri2, null, state) must not be (drawable1)
    loadedDrawable.size must be (2)
  }

  @Test
  def getImage_shouldCache() {
    val loadedDrawables = mutable.Buffer.empty[Drawable]
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, context: Context) = {
        val drawable = mock[Drawable]
        loadedDrawables += drawable
        drawable
      }
    }

    val state = new State() {}
    val uri1 = mock[Uri]
    val drawable = imageLoader.getDrawable(uri1, null, state)
    drawable must be(loadedDrawables.head)

    val result2 = imageLoader.getDrawable(uri1, null, state)
    loadedDrawables.size must be (1)
    result2 must be (drawable)
  }

  @Test
  def getImage_cacheShouldNotCauseOutOfMemoryError() {
    var loadCount = 0
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, context: Context) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }

    val state = new State() {}
    val uri1 = mock[Uri]
    imageLoader.getDrawable(uri1, null, state)
    loadCount must be (1)

    System.gc()
    imageLoader.getDrawable(uri1, null, state)
    loadCount must be (2)
  }

  @Test
  def getImage_cacheShouldOnlyReleaseIfNoOtherReferencesToImage() {
    var loadCount = 0
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, context: Context) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }

    val state = new State() {}
    val uri1 = mock[Uri]
    val drawable1 = imageLoader.getDrawable(uri1, null, state)
    loadCount must be (1)

    System.gc()
    imageLoader.getDrawable(uri1, null, state) must be (drawable1)
    loadCount must be (1)
  }
}
