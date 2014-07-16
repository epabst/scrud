package com.github.scrud.android

import com.github.scrud.EntityNavigation
import java.util.concurrent.{TimeUnit, ExecutorService, ConcurrentLinkedQueue}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.duration.Duration
import com.github.scrud.android.persistence.EntityTypeMapWithBackupForTesting
 import org.robolectric.Robolectric
 import com.github.scrud.util.Debug

 /**
 * An approximation to a scrud-enabled Android Application for use when testing.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplicationForRobolectric(entityNavigation: EntityNavigation) extends CrudAndroidApplication(entityNavigation) {
  def this() {
    this(new EntityNavigation(EntityTypeMapWithBackupForTesting))
  }

  private val scheduledFutures = new ConcurrentLinkedQueue[Future[Any]]()
  private val failures = new ConcurrentLinkedQueue[Throwable]()

  private[android] def toExecutionContext(executorService: ExecutorService): ExecutionContext = {
    ExecutionContext.fromExecutorService(executorService, throwable => failures.add(throwable))
  }
  
  private[android] def runAndAddFuture[T](body: => T)(executionContext: ExecutionContext): Future[T] = {
    if (Debug.threading) debug("Scheduling future (in CrudAndroidApplicationForRobolectric)")
    val future = Future {
      if (Debug.threading) debug("Running future (in CrudAndroidApplicationForRobolectric)")
      val result = try {
        body
      } finally {
        if (Debug.threading) debug("Done running future (in CrudAndroidApplicationForRobolectric)")
      }
      result
    }(executionContext)
    if (Debug.threading) debug("Done scheduling future (in CrudAndroidApplicationForRobolectric)")
    scheduledFutures.add(future)
    future
  }

  override def future[T](body: => T) = {
    runAndAddFuture(body)(Implicits.global)
  }

  def waitUntilIdle() {
    if (Debug.threading) debug("Waiting until idle")
    while (Robolectric.getUiThreadScheduler.runOneTask() || Robolectric.getBackgroundScheduler.runOneTask()) {}
    if (!failures.isEmpty) {
      throw failures.peek()
    }
    while (!scheduledFutures.isEmpty) {
      val future = scheduledFutures.peek()
      Await.result(future, Duration(2, TimeUnit.MINUTES))
      scheduledFutures.remove(future)

      while (Robolectric.getUiThreadScheduler.runOneTask() || Robolectric.getBackgroundScheduler.runOneTask()) {}
      if (!failures.isEmpty) {
        throw failures.peek()
      }
    }
    if (Debug.threading) debug("Done waiting until idle")
  }
}
