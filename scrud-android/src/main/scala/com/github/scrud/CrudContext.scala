package com.github.scrud

import com.github.triangle.Field
import com.github.triangle.PortableField._
import persistence.{DataListener, CrudPersistence}
import platform.PlatformDriver
import state.State
import util.ListenerHolder

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of an Application.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait CrudContext {
  def application: CrudApplication

  def platformDriver: PlatformDriver

  def applicationState: State

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  def dataListenerHolder(entityType: EntityType): ListenerHolder[DataListener] =
    application.persistenceFactory(entityType).listenerHolder(entityType, this)

  def openEntityPersistence(entityType: EntityType): CrudPersistence =
    application.persistenceFactory(entityType).createEntityPersistence(entityType, this)

  def withEntityPersistence[T](entityType: EntityType)(f: CrudPersistence => T): T = {
    val persistence = openEntityPersistence(entityType)
    try f(persistence)
    finally persistence.close()
  }
}

case class SimpleCrudContext(application: CrudApplication, platformDriver: PlatformDriver) extends CrudContext {
  val applicationState = new State {}

  /** The ISO 2 country such as "US". */
  def isoCountry = java.util.Locale.getDefault.getCountry
}

object CrudContextField extends Field(identityField[CrudContext])
