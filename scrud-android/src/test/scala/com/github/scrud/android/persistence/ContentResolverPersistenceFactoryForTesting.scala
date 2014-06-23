package com.github.scrud.android.persistence

import com.github.scrud.persistence.PersistenceFactory
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import com.github.scrud.android.view.AndroidConversions._
import com.github.scrud.android.CrudAndroidApplication

/**
 * A [[com.github.scrud.android.persistence.ContentResolverPersistenceFactory]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
class ContentResolverPersistenceFactoryForTesting(delegate: PersistenceFactory, application: CrudAndroidApplication)
    extends ContentResolverPersistenceFactory(delegate) {
  val contentProvider = new CrudContentProviderForTesting(application)
  ShadowContentResolver.registerProvider(authorityFor(application.applicationName), contentProvider)
}
