package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Logging
import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.util.{MutableListenerSet, DelegatingListenerSet}
import com.github.scrud.context.SharedContext

/**
 * CrudPersistnece that uses [[com.github.scrud.persistence.ThinPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class CrudPersistenceUsingThin(val entityType: EntityType, val thinPersistence: ThinPersistence, val sharedContext: SharedContext,
                              protected val listenerSet: MutableListenerSet[DataListener] = new MutableListenerSet[DataListener])
  extends CrudPersistence with DelegatingListenerSet[DataListener] with Logging {

  def findAll(uri: UriPath): Seq[AnyRef] = thinPersistence.findAll(uri)

  def newWritable() = thinPersistence.newWritable()

  protected[persistence] def doSave(idOption: Option[ID], writable: AnyRef): ID = {
    val targetIdFieldOpt = entityType.idField.findTargetField(targetType)
    val writableWithIdOpt: Option[AnyRef] = for {
      id <- idOption
      targetIdField <- targetIdFieldOpt
    } yield targetIdField.updateValue(writable, Some(id), sharedContext.asStubRequestContext)
    val writableToSave = writableWithIdOpt.getOrElse(writable)

    val id = thinPersistence.save(idOption, writableToSave)
    if (idOption.isEmpty) {
      targetIdFieldOpt.foreach(_.updateValue(writable, Some(id), sharedContext.asStubRequestContext))
    }
    id
  }

  def doDelete(uri: UriPath): Int = {
    thinPersistence.delete(uri)
  }

  def close() {
    thinPersistence.close()
  }
}
