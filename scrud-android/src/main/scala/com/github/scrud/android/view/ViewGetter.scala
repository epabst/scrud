package com.github.scrud.android.view

import android.view.View
import com.github.triangle.{PortableField, TargetedGetter}

/**
 * A Getter that operates on a View. Most interestingly, the withSetter uses [[com.github.scrud.android.view.ViewSetter]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/2/13
 *         Time: 4:00 PM
 */
case class ViewGetter[V <: View,T](body: V => Option[T])(implicit subjectManifest: ClassManifest[V]) extends TargetedGetter[V,T](body) {
  override def withSetter(body: V => Option[T] => Unit): PortableField[T] = {
    this + ViewSetter(body)
  }

  override val toString = "ViewGetter"
}

