package com.github.scrud.android.persistence

import com.github.scrud.android.CrudAndroidApplication
import com.github.scrud.state.State

/**
 * This is a CommandContextProvider that assumes that the application it's running in is a CrudAndroidApplication.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/20/13
 * Time: 5:24 PM
 */
class LocalCrudContentProvider extends CrudContentProvider {
  override lazy val androidApplication = getContext.getApplicationContext.asInstanceOf[CrudAndroidApplication]

  override def applicationState: State = commandContext.applicationState
}

