package com.github.scrud.persistence

import com.github.scrud.{EntityType, UriPath, EntityName}
import com.github.scrud.platform.PlatformDriver
import scala.collection.mutable

/**
 * A stateless mapping between a set of EntityTypes and the PersistenceFactory for each one.
 * Each subclass should call the entityType(EntityType, PersistenceFactory) method for each EntityType.
 * Ideally each subclass won't assume the platform (e.g. android) so that it can be re-used for multiple platforms.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */

abstract class EntityTypeMap(platformDriver: PlatformDriver) {
  private[this] val entityTypesAndFactoriesBuffer: mutable.Buffer[(EntityType, PersistenceFactory)] = mutable.Buffer[(EntityType, PersistenceFactory)]()

  lazy val entityTypesAndFactories: Seq[(EntityType, PersistenceFactory)] = entityTypesAndFactoriesBuffer.toList

  protected def entityType(entityType: EntityType, persistenceFactory: PersistenceFactory) {
    entityTypesAndFactoriesBuffer += entityType -> persistenceFactory
  }
  
  final lazy val allEntityTypes: Seq[EntityType] = entityTypesAndFactories.map(_._1)

  private[this] lazy val persistenceFactoryByEntityType: Map[EntityType, PersistenceFactory] = Map(entityTypesAndFactories: _*)

  private lazy val entityTypeByEntityName: Map[EntityName, EntityType] = {
    if (allEntityTypes.isEmpty) throw new IllegalStateException("no EntityTypes defined.  Call entityType(EntityType, PersistenceFactory) for each one.")
    val entityTypeByEntityName = allEntityTypes.map(entityType => (entityType.entityName, entityType)).toMap
    if (entityTypeByEntityName.size < entityTypesAndFactories.size) {
      val duplicates: Seq[String] = allEntityTypes.groupBy(_.entityName).filter(_._2.size > 1).keys.toSeq.map(_.name).sorted
      throw new IllegalArgumentException("EntityType names must be unique.  Duplicates=" + duplicates.mkString(","))
    }
    entityTypeByEntityName
  }

  def persistenceFactory(entityType: EntityType): PersistenceFactory = persistenceFactoryByEntityType.apply(entityType)

  /** Marked final since only a convenience method for the other [[com.github.scrud.persistence.EntityTypeMap.persistenceFactory]] method. */
  final def persistenceFactory(entityName: EntityName): PersistenceFactory = persistenceFactory(entityType(entityName))

  def entityType(entityName: EntityName): EntityType = findEntityType(entityName).getOrElse {
    throw new IllegalArgumentException("Unknown entity: entityName=" + entityName)
  }

  def findEntityType(entityName: EntityName): Option[EntityType] = entityTypeByEntityName.get(entityName)

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

object EntityTypeMap {
  def apply(entityTypesAndFactories: (EntityType, PersistenceFactory)*): EntityTypeMap = {
    val _entityTypesAndFactories = entityTypesAndFactories
    val entityTypeMap = new EntityTypeMap(entityTypesAndFactories.head._1.platformDriver) {
      override lazy val entityTypesAndFactories = _entityTypesAndFactories
    }
    entityTypeMap.entityTypeByEntityName
    entityTypeMap
  }
}
