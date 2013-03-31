package com.github.scrud.android.state

import com.github.scrud.CrudContext

/** Similar to ActivityVar but allows specifying an initial value, evaluated when first accessed. */
class LazyActivityVal[T](lazyExpression: => T) {
  private val activityVar = new ActivityVar[T]

  /** Gets the value, evaluating if needed.
    * @param crudContext the CrudContext where the value is stored
    * @return the value
    */
  def get(crudContext: CrudContext): T = {
    activityVar.getOrSet(crudContext.stateHolder, lazyExpression)
  }
}
