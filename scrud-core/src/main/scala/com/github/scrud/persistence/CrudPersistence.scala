package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Logging
import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.{IdPk, UriPath, EntityType}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.copy.{TargetType, SourceType, InstantiatingTargetType}
import com.github.scrud.platform.node.Persistence

/** An EntityPersistence for a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
@MicrotestCompatible(use = "new CrudPersistenceUsingThin")
trait CrudPersistence extends EntityPersistence with ListenerSet[DataListener] with Logging {
  override protected lazy val logTag: String = Common.tryToEvaluate(entityType.logTag).getOrElse(Common.logTag)

  def entityType: EntityType

  def sourceType: SourceType = Persistence

  def targetType: TargetType = Persistence

  override def toUri(id: ID) = entityType.toUri(id)

  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Option[T] =
    find(uri).map(entityType.copyAndUpdate(sourceType, _, targetType))

  /** Find an entity with a given ID using a baseUri. */
  def find(id: ID, baseUri: UriPath): Option[AnyRef] = find(baseUri.specify(entityType.entityName, id))

  override def find(uri: UriPath): Option[AnyRef] = {
    val result = super.find(uri)
    info("find(" + uri + ") for " + entityType.entityName + " returned " + result)
    result
  }

  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Seq[T] =
    findAll(uri).map(entityType.copyAndUpdate(Persistence, _, targetType))

  /** Saves the entity.  This assumes that the entityType's fields support copying from the given modelEntity. */
  def save(modelEntity: IdPk): ID = save(modelEntity.id, modelEntity) //todo convert to correct type

  def saveAll(modelEntityList: Seq[IdPk]): Seq[ID] = {
    modelEntityList.map(save(_))
  }

  // Available for cases where logging needs to happen outside, based on the entityType known here.
  override protected[scrud] def debug(f: => String) {
    super.debug(f)
  }

  // Available for cases where logging needs to happen outside, based on the entityType known here.
  override protected[scrud] def warn(f: => String) {
    super.warn(f)
  }

  override def toString = super.toString + " for " + entityType
}
