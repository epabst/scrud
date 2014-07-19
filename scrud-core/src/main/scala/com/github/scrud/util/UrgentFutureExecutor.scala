package com.github.scrud.util

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.LinkedBlockingDeque
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * An executor of urgent Futures.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/5/12
 * Time: 4:58 PM
 */
class UrgentFutureExecutor(maxActiveCount: Int = 20) {
  private val activeCount = new AtomicInteger(0)
  private val tasks = new LinkedBlockingDeque[FutureTask[_]]()

  /** Creates a Future that will be executed before most previously created Futures. */
  def urgentFuture[T](body: => T): Future[T] = {
    val futureTask = FutureTask(() => body, Promise[T]())
    tasks.addLast(futureTask)
    if (activeCount.get() < maxActiveCount) {
      activeCount.incrementAndGet()
      Future {
        while (!tasks.isEmpty) {
          Option(tasks.pollLast()).foreach(_.run())
        }
        activeCount.decrementAndGet()
      }
    }
    futureTask.promise.future
  }
}

private case class FutureTask[T](body: () => T, promise: Promise[T]) {
  def run() {
    try {
      val result: T = body()
      promise.success(result)
    } catch {
      case error: Throwable => promise.failure(error)
    }
  }
}
