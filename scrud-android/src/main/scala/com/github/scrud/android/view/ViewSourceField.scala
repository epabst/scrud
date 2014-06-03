package com.github.scrud.android.view

import com.github.scrud.copy.{NestedSourceField, CopyContext, TypedSourceField}
import com.github.scrud.android.AndroidCommandContext
import android.view.View

/**
 * A SourceField for an Android View.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ViewSourceField[S <: View,V] extends TypedSourceField[S,V] {
  /** Get some value or None from the given source. */
  override final def findFieldValue(sourceData: S, context: CopyContext): Option[V] =
    findFieldValue(sourceData, context.commandContext.asInstanceOf[AndroidCommandContext])

  /** Get some value or None from the given source. */
  def findFieldValue(sourceData: S, context: AndroidCommandContext): Option[V]

  def forSourceView(viewSpecifier: ViewSpecifier): NestedSourceField[V] =
    new NestedSourceField[V](viewSpecifier, this)
}
