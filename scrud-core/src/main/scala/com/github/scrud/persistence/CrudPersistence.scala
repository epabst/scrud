package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Logging
import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.{IdPk, UriPath, EntityType}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.copy.{TargetType, SourceType, InstantiatingTargetType}
import com.github.scrud.platform.representation.{EntityModel, Persistence}
import com.github.scrud.context.SharedContext

/** An EntityPersistence for a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
@MicrotestCompatible(use = "new CrudPersistenceUsingThin")
trait CrudPersistence extends EntityPersistence with ListenerSet[DataListener] with Logging {
  override protected lazy val logTag: String = Common.tryToEvaluate(entityType.logTag).getOrElse(Common.logTag)

  def entityType: EntityType

  def sharedContext: SharedContext

  def sourceType: SourceType = Persistence

  def targetType: TargetType = Persistence

  override def toUri(id: ID) = entityType.toUri(id)

  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Option[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    val stubRequestContext = sharedContext.asStubRequestContext
    find(uri).map { source =>
      val target = targetType.makeTarget(stubRequestContext)
      adaptedFieldSeq.copyAndUpdate(source, target, stubRequestContext)
    }
  }

  /** Find an entity with a given ID using a baseUri. */
  def find(id: ID, baseUri: UriPath): Option[AnyRef] = find(baseUri.specify(entityType.entityName, id))

  override def find(uri: UriPath): Option[AnyRef] = {
    val result = super.find(uri)
    info("find(" + uri + ") for " + entityType.entityName + " returned " + result)
    result
  }

  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Seq[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    val stubRequestContext = sharedContext.asStubRequestContext
    findAll(uri).map { source =>
      val target = targetType.makeTarget(stubRequestContext)
      adaptedFieldSeq.copyAndUpdate(source, target, stubRequestContext)
    }
  }

  /** Saves the entity.  This assumes that the entityType's fields support copying from the given modelEntity. */
  def save(modelEntity: IdPk): ID = {
    val adaptedFieldSeq = entityType.adapt(EntityModel, Persistence)
    val writable = adaptedFieldSeq.copyAndUpdate(modelEntity, newWritable(), sharedContext.asStubRequestContext)
    save(modelEntity.id, writable)
  }

  def toWritable(sourceType: SourceType, source: AnyRef): AnyRef = {
    val target = newWritable()
    val adaptedFieldSeq = entityType.adapt(sourceType, Persistence)
    adaptedFieldSeq.copyAndUpdate(source, target, sharedContext.asStubRequestContext)
  }

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
