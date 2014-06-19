package com.github.scrud.android.view

import com.github.scrud.copy._
import com.github.scrud.android.AndroidCommandContext

/**
 * TargetField for an Android View.
 * @author Eric Pabst (epabst@gmail.com)
 * @tparam V value class or trait
 */
trait ViewTargetField[V] extends TargetField[V] {
  def forTargetView(viewSpecifier: ViewSpecifier): NestedTargetField[V] =
    new NestedViewTargetField[V](viewSpecifier, this)
}

private class NestedViewTargetField[V](viewSpecifier: ViewSpecifier, nestedField: ViewTargetField[V]) extends NestedTargetField[V](viewSpecifier, nestedField) {
  /**
   * Updates the <code>target</code> subject using the <code>valueOpt</code> for this field and some context.
   * @return the updated target, which should be the target itself if mutable.
   */
  override def updateValue[T <: AnyRef](target: T, valueOpt: Option[V], context: CopyContext): T = {
    val commandContext = context.commandContext.asInstanceOf[AndroidCommandContext]
    commandContext.runOnUiThread[Unit] {
      super.updateValue(target, valueOpt, context)
    }
    target
  }
}
