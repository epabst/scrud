package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.util.ImageViewLoader
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext

/**
 * A TargetField for an image to be displayed.
 * @author Eric Pabst (epabst@gmail.com)
 */
object ImageViewTargetField
    extends TypedViewTargetField[ImageView,Uri](<ImageView android:adjustViewBounds="true"/>) {
  val imageViewLoader: ImageViewLoader = new ImageViewLoader()

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(targetImageView: ImageView, imageUriOpt: Option[Uri], commandContext: AndroidCommandContext, context: CopyContext): ImageView = {
    imageViewLoader.setImageDrawable(targetImageView, imageUriOpt, commandContext.applicationState)
    targetImageView
  }
}
