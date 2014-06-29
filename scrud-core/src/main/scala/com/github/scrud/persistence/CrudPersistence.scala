package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.{ExternalLogging, DelegateLogging, ListenerSet}
import com.github.scrud.{FieldDeclaration, UriPath, EntityType}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.copy.{CopyContext, TargetType, SourceType, InstantiatingTargetType}
import com.github.scrud.platform.representation.{EntityModelForPlatform, Persistence}
import com.github.scrud.context.{CommandContext, SharedContext}
import com.github.scrud.model.IdPk

/** An EntityPersistence for a CrudType.
  * @author Eric Pabst (epabst@gmail.com)
  */
@MicrotestCompatible(use = "new CrudPersistenceUsingThin")
trait CrudPersistence extends EntityPersistence with ListenerSet[DataListener] with DelegateLogging {
  def entityType: EntityType

  def sharedContext: SharedContext

  override protected def loggingDelegate: ExternalLogging = sharedContext.applicationName

  def sourceType: SourceType = Persistence.Latest

  def targetType: TargetType = Persistence.Latest

  override def toUri(id: ID) = entityType.toUri(id)

  private lazy val idSourceField = entityType.id.toAdaptableField.sourceFieldOrFail(sourceType)

  def idOption(source: AnyRef, sourceUri: UriPath): Option[ID] = {
    val copyContext = new CopyContext(sourceUri, null)
    idSourceField.findValue(source, copyContext)
  }

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
      val idOpt = idOption(source, uri)
      val sourceUri = UriPath.specify(uri, idOpt)
      adaptedFieldSeq.copyAndUpdate(source, sourceUri, target, commandContext)
    }
  }

  /** Find the non-empty field values for all entities matching a URI. */
  def findAll[V](uri: UriPath, field: FieldDeclaration[V],
                 commandContext: CommandContext): Seq[V] = {
    val sourceField = field.toAdaptableField.sourceFieldOrFail(sourceType)
    val copyContext = new CopyContext(uri, commandContext)
    findAll(uri).flatMap { entity =>
      sourceField.findValue(entity, copyContext)
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
