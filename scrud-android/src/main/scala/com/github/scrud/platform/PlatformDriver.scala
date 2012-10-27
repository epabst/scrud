package com.github.scrud.platform

import com.github.scrud.persistence.PersistenceFactory

/**
 * An API for an app to interact with the host platform such as Android.
 * It should be constructable without any kind of container.
 * Use subtypes of CrudContext for state that is available in a container.
 * The model is that the custom platform is implemented for all applications by
 * delegating to the CrudApplication to make business logic decisions.
 * Then the CrudApplication can call into this PlatformDriver or CrudContext for any calls it needs to make.
 * If direct access to the specific host platform is needed by a specific app, cast this
 * to the appropriate subclass, ideally using a scala match expression.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/25/12
 *         Time: 9:57 PM
 */
trait PlatformDriver {

  def localDatabasePersistenceFactory: PersistenceFactory
}
