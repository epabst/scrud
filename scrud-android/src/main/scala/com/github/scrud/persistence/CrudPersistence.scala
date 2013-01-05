package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle.Logging
import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.{IdPk, UriPath, EntityType}

/** An EntityPersistence for a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
trait CrudPersistence extends EntityPersistence with ListenerSet[DataListener] with Logging {
  override protected lazy val logTag: String = Common.tryToEvaluate(entityType.logTag).getOrElse(Common.logTag)

  def entityType: EntityType

  override def toUri(id: ID) = entityType.toUri(id)

  def find[T <: AnyRef](uri: UriPath, instantiateItem: => T): Option[T] =
    find(uri).map(entityType.fieldsIncludingIdPk.copyAndUpdate(_, instantiateItem))

  /** Find an entity with a given ID using a baseUri. */
  def find(id: ID, baseUri: UriPath): Option[AnyRef] = find(baseUri.specify(entityType.entityName, id))

  override def find(uri: UriPath): Option[AnyRef] = {
    val result = super.find(uri)
    info("find(" + uri + ") for " + entityType.entityName + " returned " + result)
    result
  }

  def findAll[T <: AnyRef](uri: UriPath, instantiateItem: => T): Seq[T] =
    findAll(uri).map(entityType.fieldsIncludingIdPk.copyAndUpdate(_, instantiateItem))

  /** Saves the entity.  This assumes that the entityType's fields support copying from the given modelEntity. */
  def save(modelEntity: IdPk): ID = {
    val writable = newWritable()
    save(modelEntity.id, entityType.copyAndUpdate(modelEntity, writable))
  }

  // Available for cases where logging needs to happen outside, based on the entityType known here.
  override protected[scrud] def debug(f: => String) {
    super.debug(f)
  }

  // Available for cases where logging needs to happen outside, based on the entityType known here.
  override protected[scrud] def warn(f: => String) {
    super.warn(f)
  }
}
