package com.github.scrud.android.persistence

import com.github.scrud.android.CrudAndroidApplicationLike
import com.github.scrud.state.State

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
class CrudContentProviderForTesting(override val androidApplication: CrudAndroidApplicationLike) extends CrudContentProvider {
  val applicationState = new State
}
