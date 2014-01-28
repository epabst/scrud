package com.github.scrud.util

import scala.concurrent.Future

/** A Future that is ready.
  * @author Eric Pabst (epabst@gmail.com)
  */
object ReadyFuture {
  def apply[T](value: T): Future[T] = Future.successful(value)
}
