package com.github.scrud.copy

import com.github.scrud.UriPath

/**
 * A copy from a source using an [[com.github.scrud.copy.AdaptedFieldSeq]].
 * @author epabst@gmail.com on 5/10/14.
 */
class AdaptedValueSeq(val sourceUri: UriPath, adaptedValues: Seq[BaseAdaptedValue]) {
  def update[T <: AnyRef](target: T): T = {
    var result: T = target
    for {
      adaptedValue <- adaptedValues
    } result = adaptedValue.update(result)
    result
  }
}

object AdaptedValueSeq {
  val empty: AdaptedValueSeq = new AdaptedValueSeq(UriPath.EMPTY, Seq.empty)
}
