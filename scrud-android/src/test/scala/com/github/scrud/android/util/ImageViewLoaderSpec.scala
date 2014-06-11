package com.github.scrud.android.util

import org.junit.runner.RunWith
import com.github.scrud.android.CustomRobolectricTestRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.junit.Test
import com.github.scrud.state.State
import android.net.Uri
import android.content.Context
import collection.mutable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.content.res.Resources
import android.util.DisplayMetrics
import org.mockito.Matchers

/**
 * Behavior specification for [[com.github.scrud.android.util.ImageLoader]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/6/13
 * Time: 3:38 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class ImageViewLoaderSpec extends MustMatchers with MockitoSugar {
  val screenWidth = 400
  val screenHeight = 300

  @Test
  def setImageDrawable_shouldUseImageViewSizeIfProvided() {
    val imageDisplayWidth = 40
    val imageDisplayHeight = 30
    val imageLoader = mock[ImageLoader]
    val imageView = mock[ImageView]
    val context: Context = createMockContextWithDisplayMetrics()
    when(imageView.getWidth).thenReturn(imageDisplayWidth)
    when(imageView.getHeight).thenReturn(imageDisplayHeight)
    when(imageView.getContext).thenReturn(context)

    val imageViewLoader = new ImageViewLoader(imageLoader)
    val state = new State
    val uri1 = mock[Uri]
    imageViewLoader.setImageDrawable(imageView, Some(uri1), state)
    verify(imageLoader).loadDrawable(Matchers.eq(uri1), Matchers.eq(imageDisplayWidth), Matchers.eq(imageDisplayHeight), Matchers.any())
  }

  @Test
  def setImageDrawable_shouldUseScreenSizeIfImageViewSizeIsNotSet() {
    val imageLoader = mock[ImageLoader]
    val imageView = mock[ImageView]
    val context: Context = createMockContextWithDisplayMetrics()
    when(imageView.getWidth).thenReturn(0)
    when(imageView.getHeight).thenReturn(0)
    when(imageView.getContext).thenReturn(context)

    val imageViewLoader = new ImageViewLoader(imageLoader)
    val state = new State
    val uri1 = mock[Uri]
    imageViewLoader.setImageDrawable(imageView, Some(uri1), state)
    verify(imageLoader).loadDrawable(Matchers.eq(uri1), Matchers.eq(screenWidth), Matchers.eq(screenHeight), Matchers.any())
  }

  private def createMockContextWithDisplayMetrics(): Context = {
    val context = mock[Context]
    val resources = mock[Resources]
    when(context.getResources).thenReturn(resources)
    val displayMetrics = new DisplayMetrics()
    when(resources.getDisplayMetrics).thenReturn(displayMetrics)
    displayMetrics.widthPixels = screenWidth
    displayMetrics.heightPixels = screenHeight
    context
  }

  @Test
  def getImage_cacheShouldDistinguishByUri() {
    val loadedDrawables = mutable.Buffer.empty[Drawable]
    val imageLoader = new ImageLoader() {
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, contentResolver: RichContentResolver) = {
        val drawable = mock[Drawable]
        loadedDrawables += drawable
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State
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
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, contentResolver: RichContentResolver) = {
        val drawable = mock[Drawable]
        loadedDrawables += drawable
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State
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
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, contentResolver: RichContentResolver) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State
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
      override def loadDrawable(uri: Uri, displayWidth: Int, displayHeight: Int, contentResolver: RichContentResolver) = {
        val drawable = mock[Drawable]
        loadCount += 1
        drawable
      }
    }
    val imageViewLoader = new ImageViewLoader(imageLoader)

    val state = new State
    val uri1 = mock[Uri]
    val drawable1 = imageViewLoader.getDrawable(uri1, 100, 100, null, state)
    loadCount must be (1)

    System.gc()
    imageViewLoader.getDrawable(uri1, 100, 100, null, state) must be (drawable1)
    loadCount must be (1)
  }
}
