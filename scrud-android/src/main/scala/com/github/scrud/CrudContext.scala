package com.github.scrud

import com.github.triangle.{Logging, Field}
import com.github.triangle.PortableField._
import persistence.{DataListener, CrudPersistence}
import platform.PlatformDriver
import state.State
import util.ListenerHolder
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of an Application.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait CrudContext extends Notification with Logging {
  protected def logTag = application.logTag

  def application: CrudApplication

  def platformDriver: PlatformDriver

  def applicationState: State

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private val workInProgress: mutable.ConcurrentMap[() => _,Unit] = new ConcurrentHashMap[() => _,Unit]()

  def future[T](body: => T): () => T = {
    // Use this instead of scala.actors.Futures.future because it preserves exceptions
    scala.concurrent.ops.future(trackWorkInProgress(propagateWithExceptionReporting(body))())
  }

  def trackWorkInProgress[T](body: => T): () => T = {
    val functionInProgress = new Function0[T]() {
      def apply() = try { body } finally { workInProgress.remove(this) }
    }
    workInProgress.put(functionInProgress, Unit)
    functionInProgress
  }

  def waitForWorkInProgress() {
    val start = System.currentTimeMillis()
    workInProgress.keys.foreach(_.apply())
    debug("Waited for work in progress for " + (System.currentTimeMillis() - start) + "ms")
  }

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
  lazy val isoCountry = java.util.Locale.getDefault.getCountry

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String) {
    println("Message to User: " + message)
  }
}

object CrudContextField extends Field(identityField[CrudContext])
