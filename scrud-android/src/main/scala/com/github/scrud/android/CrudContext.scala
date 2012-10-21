package com.github.scrud.android

import action.ContextWithState
import com.github.scrud.state._
import com.github.triangle.Field
import com.github.triangle.PortableField._
import android.os.Bundle
import com.github.scrud.{EntityType, CrudApplication}
import com.github.scrud.persistence.CrudPersistence
import state.{CachedStateListeners, CachedStateListener}

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of a single Activity.
 * @author Eric Pabst (epabst@gmail.com)
 */
case class CrudContext(activityContext: ContextWithState, application: CrudApplication) {
  lazy val platformDriver = new AndroidPlatformDriver(activityContext, application.logTag)
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

















