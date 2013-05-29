package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle.Logging
import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.util.{MutableListenerSet, DelegatingListenerSet}

/**
 * CrudPersistnece that uses [[com.github.scrud.persistence.ThinPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class CrudPersistenceUsingThin(val entityType: EntityType, val thinPersistence: ThinPersistence,
                              protected val listenerSet: MutableListenerSet[DataListener] = new MutableListenerSet[DataListener])
  extends CrudPersistence with DelegatingListenerSet[DataListener] with Logging {

  def findAll(uri: UriPath): Seq[AnyRef] = thinPersistence.findAll(uri)

  def newWritable() = thinPersistence.newWritable()

  def doSave(idOption: Option[ID], writable: AnyRef): ID = {
    thinPersistence.save(idOption, writable)
  }

  def doDelete(uri: UriPath): Int = {
    thinPersistence.delete(uri)
  }

  def close() {
    thinPersistence.close()
  }
}
