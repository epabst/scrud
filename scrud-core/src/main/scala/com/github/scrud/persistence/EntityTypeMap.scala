package com.github.scrud.persistence

import com.github.scrud.{UriPath, EntityType, EntityName}

/**
 * A stateless mapping between a set of EntityTypes and the PersistenceFactory for each one.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */

case class EntityTypeMap(entityTypesAndFactories: (EntityType, PersistenceFactory)*) {
  val allEntityTypes: Seq[EntityType] = entityTypesAndFactories.map(_._1)

  private val persistenceFactoryByEntityType: Map[EntityType, PersistenceFactory] = Map(entityTypesAndFactories: _*)

  private val entityTypeByEntityName: Map[EntityName, EntityType] =
    allEntityTypes.map(entityType => (entityType.entityName, entityType)).toMap

  if (entityTypeByEntityName.size < entityTypesAndFactories.size) {
    val duplicates: Seq[String] = allEntityTypes.groupBy(_.entityName).filter(_._2.size > 1).keys.toSeq.map(_.name).sorted
    throw new IllegalArgumentException("EntityType names must be unique.  Duplicates=" + duplicates.mkString(","))
  }

  def persistenceFactory(entityType: EntityType): PersistenceFactory = persistenceFactoryByEntityType.apply(entityType)

  /** Marked final since only a convenience method for the other [[com.github.scrud.persistence.EntityTypeMap.persistenceFactory]] method. */
  final def persistenceFactory(entityName: EntityName): PersistenceFactory = persistenceFactory(entityType(entityName))

  def entityType(entityName: EntityName): EntityType = entityTypeByEntityName.get(entityName).getOrElse {
    throw new IllegalArgumentException("Unknown entity: entityName=" + entityName)
  }

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance. */
  def maySpecifyEntityInstance(uri: UriPath, entityType: EntityType): Boolean =
    persistenceFactory(entityType).maySpecifyEntityInstance(entityType.entityName, uri)

  def isListable(entityType: EntityType): Boolean = persistenceFactory(entityType).canList
  def isListable(entityName: EntityName): Boolean = persistenceFactory(entityName).canList

  def isSavable(entityType: EntityType): Boolean = persistenceFactory(entityType).canSave
  def isSavable(entityName: EntityName): Boolean = persistenceFactory(entityName).canSave

  def isCreatable(entityName: EntityName): Boolean = persistenceFactory(entityName).canCreate
  def isCreatable(entityType: EntityType): Boolean = persistenceFactory(entityType).canCreate

  def isDeletable(entityType: EntityType): Boolean = persistenceFactory(entityType).canDelete
  def isDeletable(entityName: EntityName): Boolean = persistenceFactory(entityName).canDelete
}
