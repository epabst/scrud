package com.github.scala.android.crud

import action.{InitializedContextVar, ContextVars, ContextWithVars}
import com.github.triangle.Field
import com.github.triangle.PortableField._
import common.UriPath
import persistence.EntityType
import android.os.Bundle
import collection.mutable
import java.util.concurrent.CopyOnWriteArraySet
import collection.JavaConversions._

/** A context which can store data for the duration of a single Activity.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CrudContext(context: ContextWithVars, application: CrudApplication) {
  def vars: ContextVars = context

  def openEntityPersistence(entityType: EntityType): CrudPersistence =
    application.crudType(entityType).openEntityPersistence(this)

  /** This is final so that it will call the similar method even when mocking, making mocking easier when testing. */
  final def withEntityPersistence[T](entityType: EntityType)(f: CrudPersistence => T): T =
    withEntityPersistence_uncurried(entityType, f)

  /** This is useful for unit testing because it is much easier to mock than its counterpart. */
  def withEntityPersistence_uncurried[T](entityType: EntityType, f: CrudPersistence => T): T =
    application.crudType(entityType).withEntityPersistence(this)(f)

  def addCachedStateListener(listener: CachedStateListener) {
    CachedStateListeners.get(context) += listener
  }

  def onSaveState(context: ContextVars, outState: Bundle) {
    CachedStateListeners.get(context).foreach(_.onSaveState(outState))
  }

  def onRestoreState(context: ContextVars, savedState: Bundle) {
    CachedStateListeners.get(context).foreach(_.onRestoreState(savedState))
  }

  def onClearState(context: ContextVars, stayActive: Boolean) {
    CachedStateListeners.get(context).foreach(_.onClearState(stayActive))
  }
}

object CrudContextField extends Field(identityField[CrudContext])
object UriField extends Field(identityField[UriPath])

/** A listener for when a CrudContext is being destroyed and resources should be released. */
trait DestroyContextListener {
  def onDestroyContext()
}

/** Listeners that represent state and will listen to a various events. */
object CachedStateListeners extends InitializedContextVar[mutable.Set[CachedStateListener]](new CopyOnWriteArraySet[CachedStateListener]())

trait CachedStateListener {
  /** Save any cached state into the given bundle before switching context. */
  def onSaveState(outState: Bundle)

  /** Restore cached state from the given bundle before switching back context. */
  def onRestoreState(savedInstanceState: Bundle)

  /** Drop cached state.  If stayActive is true, then the state needs to be functional. */
  def onClearState(stayActive: Boolean)
}
