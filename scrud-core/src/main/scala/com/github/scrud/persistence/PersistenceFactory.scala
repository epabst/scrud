package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath, EntityType}
import com.github.scrud.util.ListenerHolder
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.context.SharedContext

/** A factory for EntityPersistence specific to a storage type such as SQLite.
  * It shouldn't define code for any of its overridable methods to avoid bugs in [[com.github.scrud.persistence.DelegatingPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@MicrotestCompatible(use = "PersistenceFactoryForTesting")
abstract class PersistenceFactory {
  /** Indicates if an entity can be saved. */
  def canSave: Boolean

  /** Indicates if an entity can be deleted. */
  def canDelete: Boolean

  /**
   * Indicates if an entity can be created.
   * It uses canDelete because it assumes that if it can be deleted, it can be created as well.
   * canCreate uses canDelete because if canList is false, then canDelete is more relevant than canCreate.
   */
  def canCreate: Boolean

  /** Indicates if an entity can be listed. */
  def canList: Boolean

  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The EntityType must support copying into this object.
    */
  def newWritable(): AnyRef

  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): CrudPersistence

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance.
    * It may be overridden in cases where an entity instance can be found even if no ID is present in the URI.
    */
  def maySpecifyEntityInstance(entityName: EntityName, uri: UriPath): Boolean

  final def addListener(listener: DataListener, entityType: EntityType, sharedContext: SharedContext) {
    listenerHolder(entityType, sharedContext).addListener(listener)
  }

  def listenerHolder(entityType: EntityType, sharedContext: SharedContext): ListenerHolder[DataListener]
}
