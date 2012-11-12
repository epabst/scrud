package com.github.scrud.util

import actors.{Future, Futures}
import java.util.concurrent.atomic.{AtomicReference, AtomicInteger}
import Futures._
import java.util.concurrent.{LinkedBlockingDeque, ConcurrentLinkedQueue, CountDownLatch}

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
    val futureTask = FutureTask(() => body, new MyFuturePromise[T])
    tasks.addLast(futureTask)
    if (activeCount.get() < maxActiveCount) {
      activeCount.incrementAndGet()
      future {
        while (!tasks.isEmpty) {
          Option(tasks.pollLast()).foreach(_.run())
        }
        activeCount.decrementAndGet()
      }
    }
    futureTask.promise
  }
}

private case class FutureTask[T](body: () => T, promise: MyFuturePromise[T]) {
  def run() {
    try {
      val result: T = body()
      promise.success(result)
    } catch {
      case error: Throwable => promise.failure(error)
    }
  }
}

/** A Promise and its Future. Refactor once using a version of Scala that has a Promise. */
private class MyFuturePromise[T] extends Future[T] {
  private val latch = new CountDownLatch(1)
  private val result = new AtomicReference[Either[T,Throwable]]()
  private val responders = new ConcurrentLinkedQueue[(T) => Unit]()

  // not a val since dynamic
  def isSet = latch.getCount == 0

  def apply() = {
    latch.await()
    resultOrThrow
  }

  private def resultOrThrow: T = {
    result.get().fold[T](v => v, throw _)
  }

  def respond(responder: (T) => Unit) {
    responders.add(responder)
    if (isSet) {
      invokeResponders(resultOrThrow)
    }
  }

  def complete(result: Either[T, Throwable]) {
    if (isSet) throw new IllegalStateException("This has already been completed.")
    this.result.set(result)
    // Invoke all known responders at the time of completion.
    // Do this before the countDown so that responders will all be processed in a single thread
    // whether here or in respond.
    invokeResponders(resultOrThrow)
    latch.countDown()
  }

  def success(result: T) {
    complete(Left(result))
  }

  def failure(error: Throwable) {
    complete(Right(error))
  }

  private def invokeResponders(result: T) {
    while (!responders.isEmpty) {
      responders.remove().apply(result)
    }
  }

  def inputChannel = {
    throw new UnsupportedOperationException("not implemented")
  }
}
