package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath

/**
 * An EntityPersistence that cannot do write operations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 4:57 PM
 */
trait ReadOnlyPersistence extends EntityPersistence {
  def newWritable = throw new UnsupportedOperationException("write not supported")

  def doSave(id: Option[ID], data: AnyRef): ID = throw new UnsupportedOperationException("write not supported")

  def doDelete(uri: UriPath) { throw new UnsupportedOperationException("delete not supported") }

  def close() {}
}
