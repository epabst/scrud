package com.github.scrud.android.persistence

import android.content.{ContentProvider, ContentResolver}
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import com.github.scrud.android.CrudAndroidApplicationLike
import com.github.scrud.android.view.AndroidConversions

/**
 * A [[android.content.ContentResolver]] to use while for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 4:19 PM
 */
class ContentResolverForTesting(contentProviderByApplicationName: Seq[(Class[_ <: CrudAndroidApplicationLike],ContentProvider)]) extends ContentResolver(null) {
  for {(applicationClass, contentProvider) <- contentProviderByApplicationName}
    ShadowContentResolver.registerProvider(AndroidConversions.authorityFor(applicationClass), contentProvider)
}
