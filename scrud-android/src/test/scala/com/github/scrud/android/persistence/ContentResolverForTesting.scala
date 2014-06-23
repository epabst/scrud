package com.github.scrud.android.persistence

import android.content.{ContentProvider, ContentResolver}
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import com.github.scrud.android.view.AndroidConversions.authorityFor
import com.github.scrud.context.ApplicationName

/**
 * A [[android.content.ContentResolver]] to use while for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 4:19 PM
 */
class ContentResolverForTesting(contentProviderByApplicationName: Map[ApplicationName,ContentProvider]) extends ContentResolver(null) {
  for {(applicationName, contentProvider) <- contentProviderByApplicationName}
    ShadowContentResolver.registerProvider(authorityFor(applicationName), contentProvider)
}
