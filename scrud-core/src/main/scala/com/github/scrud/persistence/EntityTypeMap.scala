package com.github.scrud.persistence

import com.github.scrud.{UriPath, EntityType, EntityName}
import com.github.scrud.util.Logging

/**
 * A stateless mapping between a set of EntityTypes and the PersistenceFactory for each one.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */

abstract class EntityTypeMap extends Logging {
  def packageName: String

  def logTag: String

  def allEntityTypes: Seq[EntityType]

  def persistenceFactory(entityType: EntityType): PersistenceFactory

  /** Marked final since only a convenience method for the other [[com.github.scrud.persistence.EntityTypeMap.persistenceFactory]] method. */
  final def persistenceFactory(entityName: EntityName): PersistenceFactory = persistenceFactory(entityType(entityName))

  def entityType(entityName: EntityName): EntityType = allEntityTypes.find(_.entityName == entityName).getOrElse {
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
