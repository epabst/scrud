package com.github.scrud.state

import com.github.scrud.android.CrudContext

/** A variable whose value is stored on a per-application basis in CrudContext. */
class ApplicationVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.applicationState
}
