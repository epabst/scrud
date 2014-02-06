package com.github.scrud.util

import org.scalatest.FunSpec
import java.util.concurrent.{TimeUnit, ConcurrentLinkedQueue}
import scala.collection.JavaConversions._
import org.scalatest.matchers.MustMatchers
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A test for [[com.github.scrud.util.UrgentFutureExecutor]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/6/12
 * Time: 6:34 AM
 */
@RunWith(classOf[JUnitRunner])
class UrgentFutureExecutorSpec extends FunSpec with MustMatchers {
  private val twoSeconds = Duration(2, TimeUnit.SECONDS)

  it("must give priority to those most recently added") {
    val executor = new UrgentFutureExecutor(5)
    val list = new ConcurrentLinkedQueue[Int]()
    (1 to 20).map { i =>
      println("Queuing #" + i)
      executor.urgentFuture { println("Running #" + i); list.add(i); Thread.sleep(100); i }
    }.reverse.foreach { future => future.onComplete("Waited for #" + _.get); Await.ready(future, twoSeconds) }
    // The first 10 should be done after the last 10
    list.toList.drop(10).count(_ <= 10) must be >= 6
  }

  def println(string: String) {
    scala.Predef.println(System.currentTimeMillis() + " | " + string)
  }
}
