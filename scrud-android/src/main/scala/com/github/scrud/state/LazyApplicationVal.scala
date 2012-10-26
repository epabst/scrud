package com.github.scrud.state

import com.github.scrud.CrudContext

/** Similar to ApplicationVar but allows specifying an initial value, evaluated when first accessed. */
class LazyApplicationVal[T](lazyExpression: => T) {
  private val applicationVar = new ApplicationVar[T]

  /** Gets the value, evaluating if needed.
    * @param crudContext the CrudContext where the value is stored
    * @return the value
    */
  def get(crudContext: CrudContext): T = {
    applicationVar.getOrSet(crudContext, lazyExpression)
  }
}
