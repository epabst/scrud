package com.github.scrud.android.persistence

import com.github.scrud.persistence.PersistenceFactoryMapping
import com.github.scrud.EntityType

/**
 * A [[com.github.scrud.persistence.PersistenceFactoryMapping]] that forces all calls to go through the ContentResolver.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/4/13
 * Time: 3:22 PM
 */
class ContentResolverPersistenceFactoryMapping(delegate: PersistenceFactoryMapping) extends PersistenceFactoryMapping {
  def allEntityTypes = delegate.allEntityTypes

  def packageName = delegate.packageName

  def persistenceFactory(entityType: EntityType) =
    new ContentResolverPersistenceFactory(delegate.persistenceFactory(entityType))

  protected def logTag = delegate.logTag
}
