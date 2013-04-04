package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath}

/**
 * A base implementation of [[com.github.scrud.persistence.PersistenceFactory]] that provides
 * default implementations of some methods.
 * @author Eric Pabst (epabst@gmail.com)
 */
abstract class AbstractPersistenceFactory extends PersistenceFactory {
  /** Indicates if an entity can be deleted. */
  def canDelete: Boolean = canSave

  /**
   * Indicates if an entity can be created.
   * It uses canDelete because it assumes that if it can be deleted, it can be created as well.
   * canCreate uses canDelete because if canList is false, then canDelete is more relevant than canCreate.
   */
  def canCreate: Boolean = canDelete

  /** Indicates if an entity can be listed. */
  def canList: Boolean = true

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance.
    * It may be overridden in cases where an entity instance can be found even if no ID is present in the URI.
    */
  def maySpecifyEntityInstance(entityName: EntityName, uri: UriPath): Boolean =
    uri.upToIdOf(entityName).isDefined
}
