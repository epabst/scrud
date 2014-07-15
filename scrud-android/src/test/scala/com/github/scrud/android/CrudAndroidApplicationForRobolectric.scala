package com.github.scrud.android

import com.github.scrud.EntityNavigation
import java.util.concurrent.{TimeUnit, ExecutorService, ConcurrentLinkedQueue}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.duration.Duration
import com.github.scrud.android.persistence.EntityTypeMapWithBackupForTesting

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
    val future = Future(body)(executionContext)
    scheduledFutures.add(future)
    future
  }

  override def future[T](body: => T) = {
    runAndAddFuture(body)(Implicits.global)
  }

  def waitUntilIdle() {
    if (!failures.isEmpty) {
      throw failures.peek()
    }
    while (!scheduledFutures.isEmpty) {
      val future = scheduledFutures.peek()
      Await.result(future, Duration(2, TimeUnit.MINUTES))
      scheduledFutures.remove(future)

      if (!failures.isEmpty) {
        throw failures.peek()
      }
    }
  }
}
