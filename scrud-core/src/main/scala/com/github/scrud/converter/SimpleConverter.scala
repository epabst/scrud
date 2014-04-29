package com.github.scrud.converter

/**
 * A simple Converter that handles conversion exceptions.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:43 AM
 */
abstract class SimpleConverter[-A,+B] extends Converter[A,B] {
  protected def attemptConvert(from: A): B

  def convert(from: A) = try { Some(attemptConvert(from)) } catch { case e: IllegalArgumentException => None }
}
