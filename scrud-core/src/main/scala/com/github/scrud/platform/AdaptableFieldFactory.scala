package com.github.scrud.platform

import com.github.scrud.copy.AdaptableFieldAndUnusedRepresentations
import com.github.scrud.EntityName
import com.github.scrud.types.QualifiedType

/**
 * A factory that turns some set of [[com.github.scrud.platform.representation.Representation]]s
 * into [[com.github.scrud.copy.AdaptableField]]s.
 * The other Representations are returned for processing by other factories.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 8:55 AM
 */
trait AdaptableFieldFactory {
  /**
   * Turns some set of [[com.github.scrud.platform.representation.Representation]]s
   * into [[com.github.scrud.copy.AdaptableField]]s.
   * The other Representations are returned for processing by other factories.
   * @param entityName the name of the entity that contains the field
   * @param fieldName the name of the field in the entity
   * @param qualifiedType the type of the field in the entity
   * @param fieldAndUnused the baseline AdaptableField and unused Representations to consider
   * @tparam V the type of the field's value
   * @return the field and the unused representations
   */
  def adapt[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V],
               fieldAndUnused: AdaptableFieldAndUnusedRepresentations[V]): AdaptableFieldAndUnusedRepresentations[V]
}
