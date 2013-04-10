package com.github.scrud.android.persistence

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.CrudApplication
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import com.github.scrud.android.view.AndroidConversions._

/**
 * A [[com.github.scrud.android.persistence.ContentResolverPersistenceFactory]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
object ContentResolverPersistenceFactoryForTesting {
  def apply(delegate: PersistenceFactory, application: CrudApplication): ContentResolverPersistenceFactory = {
    val contentProvider = new CrudContentProviderForTesting(application)
    ShadowContentResolver.registerProvider(authorityFor(application.packageName), contentProvider)
    new ContentResolverPersistenceFactory(delegate)
  }
}
