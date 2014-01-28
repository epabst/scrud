package com.github.scrud

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.PersistenceFactoryMapping

/**
 * An application that uses scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 9:01 AM
 */
abstract class CrudApplication(val platformDriver: PlatformDriver) extends PersistenceFactoryMapping {
  def logTag: String

}
