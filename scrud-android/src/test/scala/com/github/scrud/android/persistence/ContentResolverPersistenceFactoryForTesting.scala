package com.github.scrud.android.persistence

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.android.CrudAndroidApplicationLike

/**
 * A [[com.github.scrud.android.persistence.ContentResolverPersistenceFactory]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/24/13
 * Time: 12:01 AM
 */
class ContentResolverPersistenceFactoryForTesting(delegate: PersistenceFactory, application: CrudAndroidApplicationLike)
    extends ContentResolverPersistenceFactory(delegate) {
}
