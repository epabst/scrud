package com.github.scrud.copy

/**
 * A copy from a source using an [[com.github.scrud.copy.BaseAdaptedField]].
 * @author epabst@gmail.com on 5/10/14.
 */
trait BaseAdaptedValue {
  def update[T <: AnyRef](target: T): T
}
