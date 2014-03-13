package com.github.scrud.android.view

import android.net.Uri
import android.widget.ImageView
import com.github.scrud.android.res.R
import com.github.triangle._
import com.github.scrud.android.util.ImageViewLoader
import scala.xml.NodeSeq

object ImageViewField extends ImageViewField(new FieldLayout {
  val displayXml = <ImageView android:adjustViewBounds="true"/>
  val editXml = NodeSeq.Empty
}, new ImageViewLoader(), PortableField.emptyField)

/** A ViewField for an image to be displayed.
  * @author Eric Pabst (epabst@gmail.com)
  */
class ImageViewField(fieldLayout: FieldLayout, imageViewLoader: ImageViewLoader = new ImageViewLoader(),
                     delegate: PortableField[Uri] = PortableField.emptyField)
    extends ViewField[Uri](fieldLayout, delegate + Getter((v: ImageView) => ImageViewFieldHelper.imageUri(v)) +
        Setter[Uri] {
          case UpdaterInput(ViewExtractor(Some(view: ImageView)), uriOpt, CrudContextField(Some(crudContext))) =>
            imageViewLoader.setImageDrawable(view, uriOpt, crudContext.applicationState)
        })

object ImageViewFieldHelper {
  // This could be any value.  Android requires that it is some entry in R.
  val DefaultValueTagKey = R.drawable.icon

  def tagToUri(tag: Object): Option[Uri] = Option(tag.asInstanceOf[String]).map(Uri.parse(_))

  def imageUri(imageView: ImageView): Option[Uri] = tagToUri(imageView.getTag)
}
