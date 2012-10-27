package com.github.scrud.android

import com.github.scrud.platform.PlatformDriver

/**
 * A PlatformDriver for the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 10:23 AM
 */
object AndroidPlatformDriver extends PlatformDriver {
  lazy val localDatabasePersistenceFactory = new SQLitePersistenceFactory
}
