package com.github.scrud.android.persistence

import com.github.scrud.util.MutableListenerSet
import com.github.scrud.persistence.DataListener
import com.github.scrud.{EntityType, CrudApplication}

/**
 * A [[com.github.scrud.android.persistence.ContentResolverCrudPersistence]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
object ContentResolverCrudPersistenceForTesting {
  def apply(entityType: EntityType, application: CrudApplication): ContentResolverCrudPersistence = {
    val contentProvider = new CrudContentProviderForTesting(application)
    val contentResolver = new ContentResolverForTesting(Map(application -> contentProvider))
    new ContentResolverCrudPersistence(entityType, contentResolver, application,
      new MutableListenerSet[DataListener])
  }
}
