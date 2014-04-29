package com.github.scrud.converter

/**
 * A converter that delegates to any of multiple Converters.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:43 AM
 */
class CompositeDirectConverter[-A,+B](converters: List[Converter[A,B]]) extends Converter[A,B] {
  def convert(from: A): Option[B] = convert(from, converters)

  private[this] def convert(from: A, converters: Seq[Converter[A,B]]): Option[B] = {
    if (converters.isEmpty) {
      None
    } else {
      converters.head.convert(from) match {
        case None =>
          convert(from, converters.tail)
        case result =>
          result
      }
    }
  }
}
