package com.github.scrud.android.persistence

import com.github.scrud.android.CrudAndroidApplicationLike
import com.github.scrud.android.view.AndroidConversions
import com.xtremelabs.robolectric.shadows.ShadowContentResolver

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
class CrudContentProviderForRobolectric(override val androidApplication: CrudAndroidApplicationLike) extends CrudContentProvider {
  def register() {
    ShadowContentResolver.registerProvider(AndroidConversions.authorityFor(androidApplication), this)
  }
}
