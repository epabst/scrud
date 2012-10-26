package com.github.scrud.android.state

import com.github.scrud.CrudContext
import com.github.scrud.state.CrudContextVar
import com.github.scrud.android.AndroidCrudContext

/** A variable whose value is stored on a per-activity basis in CrudContext. */
class ActivityVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.asInstanceOf[AndroidCrudContext].activityState
}
