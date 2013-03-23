package com.github.scrud.android.persistence

import android.content.{ContentProvider, ContentResolver}
import com.xtremelabs.robolectric.shadows.ShadowContentResolver

/**
 * A [[android.content.ContentResolver]] to use while for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 4:19 PM
 */
class ContentResolverForTesting(contentProviderByAuthority: Map[String,ContentProvider]) extends ContentResolver(null) {
  for {(authority, contentProvider) <- contentProviderByAuthority} ShadowContentResolver.registerProvider(authority, contentProvider)
}
