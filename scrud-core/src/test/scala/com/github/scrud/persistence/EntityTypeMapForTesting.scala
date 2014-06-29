package com.github.scrud.persistence

import com.github.scrud.{ApplicationNameForTesting, EntityType}

/**
 * A factory for an EntityTypeMap for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 11:21 PM
 */
case class EntityTypeMapForTesting(persistenceFactoryByEntityType: (EntityType, PersistenceFactory)*)
    extends PrebuiltEntityTypeMap(ApplicationNameForTesting, persistenceFactoryByEntityType: _*) {

  def this(persistenceFactoryByEntityType: Map[EntityType, PersistenceFactory]) {
    this(persistenceFactoryByEntityType.toSeq: _*)
  }

  def this(entityTypes: Set[EntityType]) {
    this(entityTypes.toSeq.map { entityType => entityType -> entityType.platformDriver.localDatabasePersistenceFactory }: _*)
  }

  def this(entityType1: EntityType, otherEntityTypes: EntityType*) {
    this((entityType1 +: otherEntityTypes).toSet)
  }
}

object EntityTypeMapForTesting {
  def apply(persistenceFactoryByEntityType: Map[EntityType, PersistenceFactory]): EntityTypeMapForTesting =
    new EntityTypeMapForTesting(persistenceFactoryByEntityType)

  def apply(entityTypes: Set[EntityType]): EntityTypeMapForTesting =
    new EntityTypeMapForTesting(entityTypes)

  def apply(entityType1: EntityType, otherEntityTypes: EntityType*): EntityTypeMapForTesting = {
    new EntityTypeMapForTesting(entityType1, otherEntityTypes: _*)
  }
}