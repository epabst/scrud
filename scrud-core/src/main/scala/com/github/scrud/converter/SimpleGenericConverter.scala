package com.github.scrud.converter

import scala.util.Try

/**
 * A Simple GenericConverter.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:52 AM
 */
protected abstract class SimpleGenericConverter[-A,-B] extends GenericConverter[A,B] {
  def attemptConvertTo[T <: B](from: A)(implicit manifest: Manifest[T]): T

  def convertTo[T <: B](from: A)(implicit manifest: Manifest[T]) = Try(attemptConvertTo[T](from))
}
