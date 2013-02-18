package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.res.R
import com.github.triangle._
import com.github.scrud.CrudContextField
import xml.NodeSeq
import com.github.scrud.android.util.ImageViewLoader

object ImageViewField extends ImageViewField(new FieldLayout {
  val displayXml = <ImageView android:adjustViewBounds="true"/>
  val editXml = NodeSeq.Empty
}, new ImageViewLoader())

/** A ViewField for an image to be displayed.
  * @author Eric Pabst (epabst@gmail.com)
  */
class ImageViewField(fieldLayout: FieldLayout, imageViewLoader: ImageViewLoader = new ImageViewLoader())
    extends ViewField[Uri](fieldLayout) {
  protected def tagToUri(tag: Object): Option[Uri] = Option(tag.asInstanceOf[String]).map(Uri.parse(_))

  private def imageUri(imageView: ImageView): Option[Uri] = tagToUri(imageView.getTag)

  // This could be any value.  Android requires that it is some entry in R.
  val DefaultValueTagKey = R.drawable.icon

  protected def createDelegate: PortableField[Uri] = Getter((v: ImageView) => imageUri(v)) + Setter[Uri] {
    case UpdaterInput(ViewExtractor(Some(view: ImageView)), uriOpt, CrudContextField(Some(crudContext))) =>
      imageViewLoader.setImageDrawable(view, uriOpt, crudContext.applicationState)
  }

  /** To override, override createDelegate instead. This is because super is not available for a val. */
  protected final val delegate = createDelegate
}
