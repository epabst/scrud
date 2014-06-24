package com.github.scrud.android.persistence

import com.github.scrud.android.CrudAndroidApplication
import com.github.scrud.state.State

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
class CrudContentProviderForTesting(override val androidApplication: CrudAndroidApplication) extends CrudContentProvider {
  val applicationState = new State
}
