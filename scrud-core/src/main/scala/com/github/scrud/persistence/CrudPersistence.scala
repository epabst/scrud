package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Logging
import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.{UriPath, EntityType}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.copy.{TargetType, SourceType, InstantiatingTargetType}
import com.github.scrud.platform.representation.{EntityModelForPlatform, Persistence}
import com.github.scrud.context.{CommandContext, SharedContext}
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

  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T], commandContext: CommandContext): Option[T] =
    find(uri, targetType, targetType.makeTarget(commandContext), commandContext)

  def find[T <: AnyRef](uri: UriPath, targetType: TargetType, target: T, commandContext: CommandContext): Option[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    find(uri).map { source =>
      adaptedFieldSeq.copyAndUpdate(source, uri, target, commandContext)
    }
  }

  /** Find an entity with a given ID using a baseUri. */
  def find(id: ID, baseUri: UriPath): Option[AnyRef] = find(UriPath.specify(baseUri, entityType.entityName, id))

  override def find(uri: UriPath): Option[AnyRef] = {
    val result = super.find(uri)
    info("find(" + uri + ") for " + entityType.entityName + " returned " + result)
    result
  }

  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T],
                          commandContext: CommandContext): Seq[T] = {
    val adaptedFieldSeq = entityType.adapt(sourceType, targetType)
    findAll(uri).map { source =>
      val target = targetType.makeTarget(commandContext)
      val sourceUri = UriPath.specify(uri, entityType.findPersistedId(source, uri))
      adaptedFieldSeq.copyAndUpdate(source, sourceUri, target, commandContext)
    }
  }

  /** Saves the entity.  This assumes that the entityType's fields support copying from the given modelEntity. */
  def save(modelEntity: IdPk, commandContext: CommandContext): ID = {
    val adaptedFieldSeq = entityType.adapt(EntityModelForPlatform, Persistence.Latest)
    val writable = adaptedFieldSeq.copyAndUpdate(modelEntity, entityType.toUri(modelEntity.id), newWritable(), commandContext)
    save(modelEntity.id, writable)
  }

  def save(idOption: Option[ID], sourceType: SourceType, source: AnyRef, commandContext: CommandContext): ID =
    save(idOption, toWritable(sourceType, source, entityType.toUri(idOption), commandContext))

  def toWritable(sourceType: SourceType, source: AnyRef, sourceUri: UriPath, commandContext: CommandContext): AnyRef = {
    val target = newWritable()
    val adaptedFieldSeq = entityType.adapt(sourceType, Persistence.Latest)
    adaptedFieldSeq.copyAndUpdate(source, sourceUri, target, commandContext)
  }

  def saveAll(modelEntityList: Seq[IdPk], commandContext: CommandContext): Seq[ID] = {
    modelEntityList.map(save(_, commandContext))
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
