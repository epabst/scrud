package com.github.scrud.persistence

import com.github.scrud.EntityType

/**
 * A factory for an EntityTypeMap for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 11:21 PM
 */
object EntityTypeMapForTesting {
  def apply(persistenceFactoriesForTesting: PersistenceFactoryForTesting*): EntityTypeMap = {
    EntityTypeMap(persistenceFactoriesForTesting.map(_.toTuple): _*)
  }

  def apply(persistenceFactoryByEntityType: Map[EntityType, PersistenceFactory]): EntityTypeMap = {
    EntityTypeMap(persistenceFactoryByEntityType.toSeq: _*)
  }
}
