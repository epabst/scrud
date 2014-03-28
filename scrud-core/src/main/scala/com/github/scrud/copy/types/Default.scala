package com.github.scrud.copy.types

import com.github.scrud.copy._
import com.github.scrud.context.CommandContext

/**
 * A GeneratedField that specifies a value.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/6/14
 *         Time: 3:12 PM
 */
case class Default[V](valueOpt: Option[V]) extends Calculation[V] {
  /** Get some value or None from the given source. */
  def calculate(context: CommandContext) = valueOpt
}

object Default {
  def apply[V](value: V): Default[V] = {
    apply(Some(value))
  }
}
