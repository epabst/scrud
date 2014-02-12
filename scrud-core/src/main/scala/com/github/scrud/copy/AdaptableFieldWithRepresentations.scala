package com.github.scrud.copy

import com.github.scrud.platform.representation.Representation

/**
 * An [[com.github.scrud.copy.AdaptableField]] and a Set of [[com.github.scrud.platform.representation.Representation]]s it includes.
 * This is useful when building an AdaptableField from a Seq of representations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 8:53 AM
 */
case class AdaptableFieldWithRepresentations[V](field: ExtensibleAdaptableField[V], representations: Set[Representation]) {
  def orElse(other: AdaptableFieldWithRepresentations[V]): AdaptableFieldWithRepresentations[V] =
    AdaptableFieldWithRepresentations(field.orElse(other.field), representations ++ other.representations)
}
