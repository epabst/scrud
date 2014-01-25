package com.github.scrud

import action.Undoable
import com.github.triangle.{Logging, Field}
import com.github.triangle.PortableField._
import persistence.{PersistenceFactory, PersistenceFactoryMapping, DataListener, CrudPersistence}
import platform.{PlatformTypes, PlatformDriver}
import com.github.scrud.state.{SimpleStateHolder, StateHolder}
import com.github.scrud.util.ListenerHolder
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.github.annotations.quality.MicrotestCompatible

/**
 * The context and state for the application code to interact with.
 * A context's stateHolder which can store data for the duration of an Application.
 * @author Eric Pabst (epabst@gmail.com)
 */
@MicrotestCompatible(use = "SimpleCrudContext(application)")
trait CrudContext extends Notification with Logging {
  protected lazy val logTag = application.logTag

  def application: CrudApplication

  def persistenceFactoryMapping: PersistenceFactoryMapping = application

  def stateHolder: StateHolder

  //final since only here as a convenience method.
  final def applicationState = stateHolder.applicationState

  def platformDriver: PlatformDriver = application.platformDriver

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private val workInProgress: mutable.ConcurrentMap[() => _,Unit] = new ConcurrentHashMap[() => _,Unit]()

  def future[T](body: => T): () => T = {
    // Would prefer to use scala.concurrent.ops.future instead of scala.actors.Futures.future because it preserves exceptions
    // However, scala.concurrent.ops.future has a problem with scala before 2.10.1 with missing sun.misc.Unsafe.throwException
    //    scala.concurrent.ops.future(trackWorkInProgress(propagateWithExceptionReporting(body))())
    scala.actors.Futures.future {
      try {
        body
      } catch {
        case t: Throwable =>
          reportError(t)
          throw t
      }
    }
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

  def newWritable(entityType: EntityType): AnyRef = persistenceFactory(entityType).newWritable()

  def dataListenerHolder(entityName: EntityName): ListenerHolder[DataListener] =
    dataListenerHolder(persistenceFactoryMapping.entityType(entityName))

  def dataListenerHolder(entityType: EntityType): ListenerHolder[DataListener] =
    persistenceFactory(entityType).listenerHolder(entityType, this)

  /** May be overidden if needed. */
  def persistenceFactory(entityName: EntityName): PersistenceFactory = persistenceFactoryMapping.persistenceFactory(entityName)

  /**
   * Marked final since it is only a convenience method for [[com.github.scrud.CrudContext.persistenceFactory(EntityName)]].
   * This makes it clear which one needs to be overridden if needed.
   */
  final def persistenceFactory(entityType: EntityType): PersistenceFactory = persistenceFactory(entityType.entityName)

  def openEntityPersistence(entityName: EntityName): CrudPersistence =
    openEntityPersistence(persistenceFactoryMapping.entityType(entityName))

  def openEntityPersistence(entityType: EntityType): CrudPersistence =
    persistenceFactory(entityType).createEntityPersistence(entityType, this)

  def withEntityPersistence[T](entityName: EntityName)(f: CrudPersistence => T): T = {
    withEntityPersistence(persistenceFactoryMapping.entityType(entityName))(f)
  }

  def withEntityPersistence[T](entityType: EntityType)(f: CrudPersistence => T): T = {
    val persistence = openEntityPersistence(entityType)
    try f(persistence)
    finally persistence.close()
  }

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}

case class SimpleCrudContext(application: CrudApplication) extends CrudContext {
  val stateHolder = new SimpleStateHolder

  /** The ISO 2 country such as "US". */
  lazy val isoCountry = java.util.Locale.getDefault.getCountry

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String) {
    println("Message to User: " + message)
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    println("Message Key to User: " + messageKey)
  }

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) {
    println("Allowed Undo: " + undoable)
  }
}
