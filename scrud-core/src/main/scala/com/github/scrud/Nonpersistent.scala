package com.github.scrud

import com.github.scrud.platform.representation.PersistenceRange

/**
 * An EntityType that is not persisted.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/7/14
 *         Time: 11:30 PM
 */
trait Nonpersistent extends EntityType {
  /**
   * Specifies the Representations that an ID has for this entity.
   * @return a Seq of Representation
   */
  override protected def idFieldRepresentations = super.idFieldRepresentations.filter {
    case _: PersistenceRange => false
    case _ => true
  }
}
