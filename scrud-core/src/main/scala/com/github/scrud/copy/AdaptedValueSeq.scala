package com.github.scrud.copy

/**
 * A copy from a source using an [[com.github.scrud.copy.AdaptedFieldSeq]].
 * @author epabst@gmail.com on 5/10/14.
 */
class AdaptedValueSeq(adaptedValues: Seq[BaseAdaptedValue]) {
  def update[T <: AnyRef](target: T): T = {
    var result: T = target
    for {
      adaptedValue <- adaptedValues
    } result = adaptedValue.update(result)
    result
  }
}

object AdaptedValueSeq {
  val empty = new AdaptedValueSeq(Seq.empty)
}
