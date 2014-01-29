package com.github.scrud

import persistence.EntityTypeMap
import platform.PlatformDriver
import com.github.scrud.state.StateHolder
import com.github.scrud.util.Logging
import collection.concurrent
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.github.annotations.quality.MicrotestCompatible
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.scrud.context.ApplicationName

/**
 * The context and state for the application code to interact with.
 * A context's stateHolder which can store data for the duration of an Application.
 * @author Eric Pabst (epabst@gmail.com)
 */
@MicrotestCompatible(use = "SimpleCrudContext(application)")
trait CrudContext extends Notification with Logging {
  protected lazy val logTag = applicationName.logTag

  def applicationName: ApplicationName = entityNavigation.applicationName

  def entityNavigation: EntityNavigation

  def entityTypeMap: EntityTypeMap = entityNavigation.entityTypeMap

  def stateHolder: StateHolder

  //final since only here as a convenience method.
  final def applicationState = stateHolder.applicationState

  def platformDriver: PlatformDriver = entityNavigation.platformDriver

  private val workInProgress: concurrent.Map[() => _,Unit] = new ConcurrentHashMap[() => _,Unit]()

  def future[T](body: => T): Future[T] = {
    scala.concurrent.future {
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
}
