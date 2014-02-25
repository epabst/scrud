package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Logging
import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.{UriPath, EntityType}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.copy.{TargetType, SourceType, InstantiatingTargetType}
import com.github.scrud.platform.representation.{EntityModel, Persistence}
import com.github.scrud.context.{RequestContext, SharedContext}
import scala.util.Try
import com.github.scrud.model.IdPk

/** An EntityPersistence for a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
@MicrotestCompatible(use = "new CrudPersistenceUsingThin")
trait CrudPersistence extends EntityPersistence with ListenerSet[DataListener] with Logging {
  override protected lazy val logTag: String = Try(entityType.logTag).getOrElse(Common.logTag)

  def entityType: EntityType

  def sharedContext: SharedContext

  def sourceType: SourceType = Persistence.Latest

  def targetType: TargetType = Persistence.Latest

  override def toUri(id: ID) = entityType.toUri(id)

  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T],
                        requestContext: RequestContext): Option[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    find(uri).map { source =>
      val target = targetType.makeTarget(requestContext)
      adaptedFieldSeq.copyAndUpdate(source, target, requestContext)
    }
  }

  /** Find an entity with a given ID using a baseUri. */
  def find(id: ID, baseUri: UriPath): Option[AnyRef] = find(baseUri.specify(entityType.entityName, id))

  override def find(uri: UriPath): Option[AnyRef] = {
    val result = super.find(uri)
    info("find(" + uri + ") for " + entityType.entityName + " returned " + result)
    result
  }

  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T],
                          requestContext: RequestContext): Seq[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    findAll(uri).map { source =>
      val target = targetType.makeTarget(requestContext)
      adaptedFieldSeq.copyAndUpdate(source, target, requestContext)
    }
  }

  /** Saves the entity.  This assumes that the entityType's fields support copying from the given modelEntity. */
  def save(modelEntity: IdPk, requestContext: RequestContext): ID = {
    val adaptedFieldSeq = entityType.adapt(EntityModel, Persistence.Latest)
    val writable = adaptedFieldSeq.copyAndUpdate(modelEntity, newWritable(), requestContext)
    save(modelEntity.id, writable)
  }

  def toWritable(sourceType: SourceType, source: AnyRef, requestContext: RequestContext): AnyRef = {
    val target = newWritable()
    val adaptedFieldSeq = entityType.adapt(sourceType, Persistence.Latest)
    adaptedFieldSeq.copyAndUpdate(source, target, requestContext)
  }

  def saveAll(modelEntityList: Seq[IdPk], requestContext: RequestContext): Seq[ID] = {
    modelEntityList.map(save(_, requestContext))
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
