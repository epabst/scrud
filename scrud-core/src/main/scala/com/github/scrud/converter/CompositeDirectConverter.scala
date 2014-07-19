package com.github.scrud.converter

import scala.util.{Try, Failure}

/**
 * A converter that delegates to any of multiple Converters.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:43 AM
 */
class CompositeDirectConverter[-A,+B](converters: List[Converter[A,B]]) extends Converter[A,B] {
  def convert(from: A): Try[B] = convert(from, converters)

  private[this] def convert(from: A, converters: Seq[Converter[A,B]]): Try[B] = {
    if (converters.isEmpty) {
      Failure(new IllegalStateException("No converters were specified."))
    } else {
      converters.head.convert(from) match {
        case f: Failure[_] =>
          convert(from, converters.tail).orElse(f)
        case result =>
          result
      }
    }
  }
}
