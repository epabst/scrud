package com.github.scrud.android

import scala.collection.mutable
import com.github.scrud.platform.PlatformTypes
import scala.concurrent.{Await, ExecutionContext, Future}
import java.util.concurrent.{TimeUnit, ConcurrentLinkedQueue, Executors}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing with Robolectric.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 6/28/14
 */
class AndroidCommandContextForRobolectric(application: CrudAndroidApplicationLike, activity: CrudActivity)
  extends AndroidCommandContext(activity, application) {

  private val scheduledFutures = new ConcurrentLinkedQueue[Future[Any]]()
  private val failures = new ConcurrentLinkedQueue[Throwable]()
  val uiThreadExecutionContext: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1),
    throwable => failures.add(throwable))

  private val entityTypesWithWrongPlatformDriver = entityTypeMap.allEntityTypes.filter(!_.platformDriver.isInstanceOf[AndroidPlatformDriver])
  if (!entityTypesWithWrongPlatformDriver.isEmpty) {
    sys.error("entityTypes=" + entityTypesWithWrongPlatformDriver + " were not instantiated with an AndroidPlatformDriver.  Use AndroidPlatformDriverForTesting.")
  }

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  override def future[T](body: => T) = {
    runAndAddFuture(body)(Implicits.global)
  }

  override def runOnUiThread[T](body: => T) {
    runAndAddFuture(body)(uiThreadExecutionContext)
  }

  private def runAndAddFuture[T](body: => T)(executionContext: ExecutionContext): Future[T] = {
    val future = Future(body)(executionContext)
    scheduledFutures.add(future)
    future
  }

  def waitUntilIdle() {
    if (!failures.isEmpty) {
      reportError(failures.peek())
    }
    while (!scheduledFutures.isEmpty) {
      val future = scheduledFutures.peek()
      Await.result(future, Duration(2, TimeUnit.MINUTES))
      scheduledFutures.remove(scheduledFutures)

      if (!failures.isEmpty) {
        reportError(failures.peek())
      }
    }
  }

  override def reportError(throwable: Throwable) {
    throw throwable
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  override def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    displayedMessageKeys += messageKey
  }
}
