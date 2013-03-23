package com.github.scrud.android.persistence

import com.github.scrud.util.MutableListenerSet
import com.github.scrud.persistence.DataListener
import com.github.scrud.CrudApplication

/**
 * A [[com.github.scrud.android.persistence.ContentResolverCrudPersistence]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
object ContentResolverCrudPersistenceForTesting {
  def apply(application: CrudApplication): ContentResolverCrudPersistence = {
    val contentProvider = new CrudContentProviderForTesting(application)
    val contentResolver = new ContentResolverForTesting(Map(application.packageName -> contentProvider))
    new ContentResolverCrudPersistence(application.allEntityTypes.head, contentResolver, application,
      new MutableListenerSet[DataListener])
  }
}
