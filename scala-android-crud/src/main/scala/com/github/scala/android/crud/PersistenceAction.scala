package com.github.scala.android.crud

import action.{UriPath, BaseAction}
import common.PlatformTypes
import android.app.Activity

/**
 * An action that interacts with an entity's persistence.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/21/11
 * Time: 6:59 AM
 */
abstract class PersistenceAction(entityType: CrudType, val application: CrudApplication,
                   icon: Option[PlatformTypes#ImgKey], title: Option[PlatformTypes#SKey])
        extends BaseAction(icon, title) {
  def invoke(uri: UriPath, persistence: CrudPersistence)

  def invoke(uri: UriPath, activity: Activity) {
    entityType.withEntityPersistence(new CrudContext(activity, application), { persistence => invoke(uri, persistence) })
  }
}

