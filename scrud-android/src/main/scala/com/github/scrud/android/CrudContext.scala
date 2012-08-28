package com.github.scrud.android

import action.{StateVar, State, ContextWithState}
import com.github.triangle.Field
import com.github.triangle.PortableField._
import common.UriPath
import persistence.EntityType
import android.os.Bundle
import collection.mutable
import java.util.concurrent.CopyOnWriteArraySet
import collection.JavaConversions._

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of a single Activity.
 * @author Eric Pabst (epabst@gmail.com)
 */
case class CrudContext(activityContext: ContextWithState, application: CrudApplication) {
  def activityState: State = activityContext
  lazy val applicationState: State = activityContext.applicationState

  def dataListenerHolder(entityType: EntityType) =
    application.crudType(entityType).persistenceFactory.listenerHolder(entityType, this)

  def openEntityPersistence(entityType: EntityType): CrudPersistence =
    application.crudType(entityType).openEntityPersistence(this)

  /** This is final so that it will call the similar method even when mocking, making mocking easier when testing. */
  final def withEntityPersistence[T](entityType: EntityType)(f: CrudPersistence => T): T =
    withEntityPersistence_uncurried(entityType, f)

  /** This is useful for unit testing because it is much easier to mock than its counterpart. */
  def withEntityPersistence_uncurried[T](entityType: EntityType, f: CrudPersistence => T): T =
    application.crudType(entityType).withEntityPersistence(this)(f)

  def addCachedActivityStateListener(listener: CachedStateListener) {
    CachedStateListeners.get(this) += listener
  }

  def onSaveActivityState(outState: Bundle) {
    CachedStateListeners.get(this).foreach(_.onSaveState(outState))
  }

  def onRestoreActivityState(savedState: Bundle) {
    CachedStateListeners.get(this).foreach(_.onRestoreState(savedState))
  }

  def onClearActivityState(stayActive: Boolean) {
    CachedStateListeners.get(this).foreach(_.onClearState(stayActive))
  }
}

object CrudContextField extends Field(identityField[CrudContext])
object UriField extends Field(identityField[UriPath])

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

/** A variable whose value is stored on a per-activity basis in CrudContext. */
class ActivityVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.activityState
}

/** A variable whose value is stored on a per-application basis in CrudContext. */
class ApplicationVar[T] extends CrudContextVar[T] {
  protected def state(crudContext: CrudContext) = crudContext.applicationState
}

/** Similar to ActivityVar but allows specifying an initial value, evaluated when first accessed. */
class LazyActivityVal[T](lazyExpression: => T) {
  private val activityVar = new ActivityVar[T]

  /** Gets the value, evaluating if needed.
    * @param crudContext the CrudContext where the value is stored
    * @return the value
    */
  def get(crudContext: CrudContext): T = {
    activityVar.getOrSet(crudContext, lazyExpression)
  }
}

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

/** A listener for when a CrudContext is being destroyed and resources should be released. */
trait DestroyStateListener {
  def onDestroyState()
}

/** Listeners that represent state and will listen to a various events. */
object CachedStateListeners extends LazyActivityVal[mutable.Set[CachedStateListener]](new CopyOnWriteArraySet[CachedStateListener]())

trait CachedStateListener {
  /** Save any cached state into the given bundle before switching context. */
  def onSaveState(outState: Bundle)

  /** Restore cached state from the given bundle before switching back context. */
  def onRestoreState(savedInstanceState: Bundle)

  /** Drop cached state.  If stayActive is true, then the state needs to be functional. */
  def onClearState(stayActive: Boolean)
}
