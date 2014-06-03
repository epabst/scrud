package com.github.scrud.android.state

import com.github.scrud.context.CommandContext
import com.github.scrud.android.AndroidCommandContext

/** Similar to ActivityVar but allows specifying an initial value, evaluated when first accessed. */
class LazyActivityVal[T](lazyExpression: => T) {
  private val activityVar = new ActivityVar[T]

  /** Gets the value, evaluating if needed.
    * @param commandContext the CommandContext where the value is stored
    * @return the value
    */
  def get(commandContext: CommandContext): T = {
    activityVar.getOrSet(commandContext.asInstanceOf[AndroidCommandContext].stateHolder, lazyExpression)
  }
}
