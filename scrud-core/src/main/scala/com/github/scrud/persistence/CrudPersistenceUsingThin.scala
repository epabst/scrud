package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.util.{ListenerSet, DelegatingListenerSet}
import com.github.scrud.context.SharedContext
import com.github.scrud.copy.{StorageType, CopyContext}

/**
 * CrudPersistnece that uses [[com.github.scrud.persistence.ThinPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 */
class CrudPersistenceUsingThin(val entityType: EntityType, val thinPersistence: ThinPersistence, val sharedContext: SharedContext,
                              protected val listenerSet: ListenerSet[DataListener])
  extends CrudPersistence with DelegatingListenerSet[DataListener] {

  def findAll(uri: UriPath): Seq[AnyRef] = thinPersistence.findAll(uri)

  def newWritable() = thinPersistence.newWritable()

  override def writableType: StorageType = thinPersistence.writableType

  protected def doSave(idOption: Option[ID], writable: AnyRef): ID = {
    val targetIdFieldOpt = entityType.idField.findTargetField(writableType)
    val sourceUri = entityType.toUri(idOption)
    val copyContext = new CopyContext(sourceUri, sharedContext.asStubCommandContext)
    val writableWithIdOpt: Option[AnyRef] = for {
      id <- idOption
      targetIdField <- targetIdFieldOpt
    } yield targetIdField.updateValue(writable, Some(id), copyContext)
    val writableToSave = writableWithIdOpt.getOrElse(writable)

    val id = thinPersistence.save(idOption, writableToSave)
    if (idOption.isEmpty) {
      targetIdFieldOpt.foreach(_.updateValue(writable, Some(id), copyContext))
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
