package com.github.scrud.platform

import com.github.scrud.copy.{Representation, AdaptableFieldWithRepresentations}
import com.github.scrud.EntityName
import com.github.scrud.types.QualifiedType

/**
 * A factory that turns some set of [[com.github.scrud.copy.Representation]]s
 * into [[com.github.scrud.copy.AdaptableField]]s.
 * The other Representations are returned for processing by other factories.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 8:55 AM
 */
trait AdaptableFieldFactory {
  /**
   * Turns some sequence of [[Representation]]s
   * into an [[com.github.scrud.copy.AdaptableField]].
   * Any Representations not included in the result are often processed by other factories.
   * @param entityName the name of the entity that contains the field
   * @param fieldName the name of the field in the entity
   * @param qualifiedType the type of the field in the entity
   * @param representations the Representations to consider adapting to
   * @tparam V the type of the field's value
   * @return the field and the representations it adapts to
   */
  def adapt[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V],
               representations: Seq[Representation]): AdaptableFieldWithRepresentations[V]
}
