package com.github.scrud.copy

import com.github.scrud.platform.representation.Representation

/**
 * An [[com.github.scrud.copy.AdaptableField]] and a Seq of unused [[com.github.scrud.platform.representation.Representation]]s.
 * This is useful when building an AdaptableField from a Seq of representations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 8:53 AM
 */
case class AdaptableFieldAndUnusedRepresentations[V](field: ExtensibleAdaptableField[V], unusedRepresentations: Seq[Representation]) {
  def representationsWithType[R <: Representation](implicit manifest: Manifest[R]): Seq[R] = {
    unusedRepresentations.collect {
      case r if manifest.unapply(r).isDefined => r.asInstanceOf[R]
    }
  }

  def orElse(orElseField: ExtensibleAdaptableField[V], representationsToRemove: Seq[Representation]): AdaptableFieldAndUnusedRepresentations[V] =
    AdaptableFieldAndUnusedRepresentations(field.orElse(orElseField), unusedRepresentations.filterNot(representationsToRemove.contains(_)))
}
