package com.github.scala.android.crud.action

import collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import android.content.Context
import collection.JavaConversions._
import android.app.Activity
import com.github.scala.android.crud.common.SimpleListenerHolder
import com.github.scala.android.crud.{CrudContext, DestroyStateListener}

/** A container for values of [[com.github.scala.android.crud.action.StateVar]]'s */
trait State extends SimpleListenerHolder[DestroyStateListener] {
  //for some reason, making this lazy results in it being null during testing, even though lazy would be preferrable.
  private[crud] val variables: ConcurrentMap[StateVar[_], Any] = new ConcurrentHashMap[StateVar[_], Any]()

  def onDestroyState() {
    listeners.foreach(_.onDestroyState())
  }
}

/** A variable stored in a [[com.github.scala.android.crud.action.State]].
  * <p />
  * Normally you create an object that extends this:
  * {{{object ProductName extends StateVar[String]}}}
  * But if you need uniqueness by instance, do this:
  * {{{val productName = new StateVar[String]}}}
  * It doesn't accumulate any data and is sharable across threads since all data is stored in each State instance.
  */
class StateVar[T] {
  /** Gets the value or None if not set.
    * @param state the State where the value is stored
    * @return Some(value) if set, otherwise None
    */
  def get(state: State): Option[T] = {
    state.variables.get(this).map(_.asInstanceOf[T])
  }

  /** Tries to set the value in {{{state}}}.
    * @param state the State where the value is stored
    * @param value the value to set in the State.
    */
  def set(state: State, value: T) {
    state.variables.put(this, value)
  }

  def clear(state: State): Option[T] = {
    state.variables.remove(this).map(_.asInstanceOf[T])
  }

  def getOrSet(state: State, initialValue: => T): T = {
    get(state).getOrElse {
      val value = initialValue
      set(state, value)
      value
    }
  }
}

/** Similar to StateVar but allows specifying an initial value, evaluated when first accessed. */
class InitializedStateVar[T](initialValue: => T) {
  private val stateVar = new StateVar[T]

  /** Gets the value or None if not set.
    * @param state the State where the value is stored
    * @return Some(value) if set, otherwise None
    */
  def get(state: State): T = {
    stateVar.getOrSet(state, initialValue)
  }
}

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

class ActivityStateVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.activityState
}

class ApplicationStateVar[T] extends CrudContextVar[T] {
  // todo use state that is shared for the whole application
  protected def state(crudContext: CrudContext) = crudContext.activityState
}

/** A State that has been mixed with a Context.
  * @author Eric Pabst (epabst@gmail.com)
  */
trait ContextWithState extends Context with State

/** A State that has been mixed with an Activity. */
trait ActivityWithState extends Activity with ContextWithState
