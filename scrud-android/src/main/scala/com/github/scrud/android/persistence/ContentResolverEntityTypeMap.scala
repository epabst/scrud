package com.github.scrud.android.persistence

import com.github.scrud.persistence.{PersistenceFactory, EntityTypeMap}
import com.github.scrud.EntityType
import com.github.scrud.util.CachedFunction
import com.github.scrud.android.AndroidCommandContext

/**
 * A [[com.github.scrud.persistence.EntityTypeMap]] that forces all calls to go through the ContentResolver.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/4/13
 * Time: 3:22 PM
 */
class ContentResolverEntityTypeMap(delegate: EntityTypeMap, commandContext: AndroidCommandContext)
    extends EntityTypeMap(delegate.applicationName, delegate.platformDriver) {

  override lazy val entityTypesAndFactories: Seq[(EntityType, PersistenceFactory)] = delegate.entityTypesAndFactories

  override def packageName = delegate.packageName

  private val cachedPersistenceFactoryByEntityType = CachedFunction { (entityType: EntityType) =>
    new ContentResolverPersistenceFactory(delegate.persistenceFactory(entityType), commandContext)
  }

  override def persistenceFactory(entityType: EntityType) = cachedPersistenceFactoryByEntityType(entityType)
}
