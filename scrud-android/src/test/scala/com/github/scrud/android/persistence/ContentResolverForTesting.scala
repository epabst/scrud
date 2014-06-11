package com.github.scrud.android.persistence

import android.content.{ContentProvider, ContentResolver}
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import com.github.scrud.CrudApplication
import com.github.scrud.android.view.AndroidConversions.authorityFor

/**
 * A [[android.content.ContentResolver]] to use while for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 4:19 PM
 */
class ContentResolverForTesting(contentProviderByApplication: Map[CrudApplication,ContentProvider]) extends ContentResolver(null) {
  for {(application, contentProvider) <- contentProviderByApplication}
    ShadowContentResolver.registerProvider(authorityFor(application.packageName), contentProvider)
}
