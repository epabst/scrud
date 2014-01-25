package com.github.scrud.util

import org.scalatest.FunSpec
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConversions._
import org.scalatest.matchers.MustMatchers

/**
 * A test for [[com.github.scrud.util.UrgentFutureExecutor]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/6/12
 * Time: 6:34 AM
 */
class UrgentFutureExecutorSpec extends FunSpec with MustMatchers {
  it("must give priority to those most recently added") {
    val executor = new UrgentFutureExecutor(5)
    val list = new ConcurrentLinkedQueue[Int]()
    (1 to 20).map { i =>
      println("Queuing #" + i)
      executor.urgentFuture { println("Running #" + i); list.add(i); Thread.sleep(100); i }
    }.reverse.foreach { future => println("Waited for #" + future.apply()) }
    // The first 10 should be done after the last 10
    list.toList.drop(10).filter(_ <= 10).size must be >= (6)
  }

  def println(string: String) {
    scala.Predef.println(System.currentTimeMillis() + " | " + string)
  }
}
