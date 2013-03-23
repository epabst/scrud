package com.github.scrud.android.persistence

import com.github.scrud.android.CrudAndroidApplication

/**
 * This is a CrudContextProvider that assumes that the application it's running in is a CrudAndroidApplication.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/20/13
 * Time: 5:24 PM
 */
class LocalCrudContentProvider extends CrudContentProvider {
  lazy val crudAndroidApplication = getContext.getApplicationContext.asInstanceOf[CrudAndroidApplication]

  def applicationState = crudAndroidApplication.applicationState

  lazy val application = crudAndroidApplication.application
}

