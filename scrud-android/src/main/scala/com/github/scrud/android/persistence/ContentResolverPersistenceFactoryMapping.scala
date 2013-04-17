package com.github.scrud.android.persistence

import com.github.scrud.persistence.PersistenceFactoryMapping
import com.github.scrud.EntityType
import com.github.scrud.util.CachedFunction

/**
 * A [[com.github.scrud.persistence.PersistenceFactoryMapping]] that forces all calls to go through the ContentResolver.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/4/13
 * Time: 3:22 PM
 */
class ContentResolverPersistenceFactoryMapping(delegate: PersistenceFactoryMapping) extends PersistenceFactoryMapping {
  def allEntityTypes = delegate.allEntityTypes

  def packageName = delegate.packageName

  private val cachedPersistenceFactoryByEntityType = CachedFunction { (entityType: EntityType) =>
    new ContentResolverPersistenceFactory(delegate.persistenceFactory(entityType))
  }

  def persistenceFactory(entityType: EntityType) = cachedPersistenceFactoryByEntityType(entityType)

  def logTag = delegate.logTag
}
