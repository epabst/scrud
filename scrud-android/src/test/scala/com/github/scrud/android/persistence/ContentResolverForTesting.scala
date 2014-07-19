package com.github.scrud.android.persistence

import android.content.{ContentProvider, ContentResolver}
import com.github.scrud.android.CrudAndroidApplicationLike

/**
 * A [[android.content.ContentResolver]] to use while for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 4:19 PM
 */
class ContentResolverForTesting(contentProviderByApplicationName: Seq[(Class[_ <: CrudAndroidApplicationLike],ContentProvider)]) extends ContentResolver(null) {
  for {(applicationClass, contentProvider) <- contentProviderByApplicationName} {
    Unit
    // Robolectric does this automatically from the manifest now:
    //   ShadowContentResolver.registerProvider(AndroidConversions.authorityFor(applicationClass), contentProvider)
  }
}
