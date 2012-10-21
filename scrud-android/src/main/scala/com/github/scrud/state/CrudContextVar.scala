package com.github.scrud.state

import com.github.scrud.android.CrudContext

/**
 * A variable stored by CrudContext.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:15 PM
 */
abstract class CrudContextVar[T] {
  private val stateVar: StateVar[T] = new StateVar[T]

  protected def state(crudContext: CrudContext): State

  def get(crudContext: CrudContext): Option[T] = stateVar.get(state(crudContext))

  /** Tries to set the value in {{{crudContext}}}.
    * @param crudContext the CrudContext where the value is stored
    * @param value the value to set in the CrudContext.
    */
  def set(crudContext: CrudContext, value: T) {
    stateVar.set(state(crudContext), value)
  }

  def clear(crudContext: CrudContext): Option[T] = {
    stateVar.clear(state(crudContext))
  }

  def getOrSet(crudContext: CrudContext, initialValue: => T): T = {
    stateVar.getOrSet(state(crudContext), initialValue)
  }
}
