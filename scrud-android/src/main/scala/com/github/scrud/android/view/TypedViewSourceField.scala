package com.github.scrud.android.view

import com.github.scrud.copy.{CopyContext, TypedSourceField}
import com.github.scrud.android.AndroidCommandContext
import android.view.View

/**
 * A SourceField for an Android View.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait TypedViewSourceField[S <: View,V] extends TypedSourceField[S,V] with ViewSourceField[V] {
  /** Get some value or None from the given source. */
  override final def findFieldValue(sourceData: S, context: CopyContext): Option[V] =
    findFieldValue(sourceData, context.commandContext.asInstanceOf[AndroidCommandContext])

  /** Get some value or None from the given source. */
  def findFieldValue(sourceData: S, context: AndroidCommandContext): Option[V]
}
