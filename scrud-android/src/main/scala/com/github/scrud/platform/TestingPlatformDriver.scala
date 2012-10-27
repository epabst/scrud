package com.github.scrud.platform

import com.github.scrud.persistence.ListBufferPersistenceFactory

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends PlatformDriver {
  protected def logTag = getClass.getSimpleName

  val localDatabasePersistenceFactory = new ListBufferPersistenceFactory[AnyRef](Map.empty[String,Any])
}

object TestingPlatformDriver extends TestingPlatformDriver
