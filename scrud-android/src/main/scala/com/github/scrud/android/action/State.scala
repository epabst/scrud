package com.github.scrud.android.action

import collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import android.content.Context
import collection.JavaConversions._
import android.app.Activity
import com.github.scrud.android.common.SimpleListenerHolder
import com.github.scrud.android.DestroyStateListener

/** A container for values of [[com.github.scrud.android.action.StateVar]]'s */
trait State extends SimpleListenerHolder[DestroyStateListener] {
  //for some reason, making this lazy results in it being null during testing, even though lazy would be preferrable.
  private[scrud] val variables: ConcurrentMap[StateVar[_], Any] = new ConcurrentHashMap[StateVar[_], Any]()

  def onDestroyState() {
    listeners.foreach(_.onDestroyState())
  }
}

/** A variable stored in a [[com.github.scrud.android.action.State]].
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
class LazyStateVal[T](lazyExpression: => T) {
  private val stateVar = new StateVar[T]

  /** Gets the value, evaluating if needed.
    * @param state the State where the value is stored
    * @return the value
    */
  def get(state: State): T = {
    stateVar.getOrSet(state, lazyExpression)
  }
}

/** A State that has been mixed with a Context.
  * @author Eric Pabst (epabst@gmail.com)
  */
trait ContextWithState extends Context with State {
  def applicationState: State
}

/** A State that has been mixed with an Activity. */
trait ActivityWithState extends Activity with ContextWithState {
  // getApplication gets the Android application, which should extend CrudAndroidApplication, which already extends State.
  def applicationState: State = getApplication.asInstanceOf[State]
}