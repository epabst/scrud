package com.github.scrud.state

import com.github.scrud.android.CrudContext

/** A variable whose value is stored on a per-activity basis in CrudContext. */
class ActivityVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.activityState
}
