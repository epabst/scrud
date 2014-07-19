package com.github.scrud.android.view

import com.github.scrud.copy.{CopyContext, TypedTargetField}
import scala.xml.NodeSeq
import com.github.scrud.android.AndroidCommandContext
import android.view.View

/**
 * TargetField for an Android View.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 * @tparam T target class or trait
 * @tparam V value class or trait
 */
abstract class TypedViewTargetField[T <: View,V](val defaultLayout: NodeSeq) extends TypedTargetField[T,V] with ViewTargetField[V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: T, valueOpt: Option[V], commandContext: AndroidCommandContext, context: CopyContext): T

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  final def updateFieldValue(target: T, valueOpt: Option[V], context: CopyContext) = {
    updateFieldValue(target, valueOpt, context.commandContext.asInstanceOf[AndroidCommandContext], context)
  }
}
