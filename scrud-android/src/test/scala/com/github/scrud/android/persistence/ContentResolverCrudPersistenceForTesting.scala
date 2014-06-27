package com.github.scrud.android.persistence

import com.github.scrud.util.MutableListenerSet
import com.github.scrud.persistence.DataListener
import com.github.scrud.EntityType
import com.github.scrud.android.{CrudAndroidApplicationLike, AndroidCommandContext}

/**
 * A [[com.github.scrud.android.persistence.ContentResolverCrudPersistence]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
class ContentResolverCrudPersistenceForTesting(entityType: EntityType, application: CrudAndroidApplicationLike,
                                               contentProvider: CrudContentProviderForTesting, commandContext: AndroidCommandContext)
  extends ContentResolverCrudPersistence(entityType, new ContentResolverForTesting(Map(application.applicationName -> contentProvider)),
    application.entityTypeMap, commandContext, new MutableListenerSet[DataListener]) {

  def this(entityType: EntityType, application: CrudAndroidApplicationLike, commandContext: AndroidCommandContext) {
    this(entityType, application, new CrudContentProviderForTesting(application), commandContext)
  }
}
