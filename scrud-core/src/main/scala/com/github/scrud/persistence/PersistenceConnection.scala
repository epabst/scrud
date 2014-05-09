package com.github.scrud.persistence

import com.github.scrud.{UriPath, FieldDeclaration, EntityType, EntityName}
import com.github.scrud.context.{CommandContext, SharedContext}
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.util.{Cache, DelegatingListenerHolder}
import com.github.scrud.platform.PlatformTypes.ID
import com.github.scrud.copy.{CopyContext, SourceType, InstantiatingTargetType}

/**
 * A pseudo-connection to any/all persistence mechanisms.
 * Actual connections to specific persistence mechanisms can be cached in the state field.
 * All actual connections that need to be closed should register themselves as listeners to the state
 * and close themselves when notified.
 * @author Eric Pabst (epabst@gmail.com)
 */
class PersistenceConnection(val entityTypeMap: EntityTypeMap, val sharedContext: SharedContext)
    extends DelegatingListenerHolder[DestroyStateListener] {

  private[persistence] val state: State = new State
  protected def listenerHolder = state

  def persistenceFor(entityName: EntityName): CrudPersistence =
    persistenceFor(entityTypeMap.entityType(entityName))

  def persistenceFor(uri: UriPath): CrudPersistence = persistenceFor(UriPath.lastEntityNameOrFail(uri))

  private val cache = new Cache()

  def persistenceFor(entityType: EntityType): CrudPersistence = cache.cacheBasedOn(entityType) {
    val entityPersistence = entityTypeMap.persistenceFactory(entityType).createEntityPersistence(entityType, this)
    state.addListener(new DestroyStateListener {
      def onDestroyState() {
        entityPersistence.close()
      }
    })
    entityPersistence
  }

  /** Indicate that the PersistenceConnection will no longer be used. */
  def close() {
    // This will delegate to any listeners.
    state.onDestroyState()
    cache.clear()
  }

  /** Find the field value for a certain entity by ID. */
  def find[V](entityName: EntityName, id: ID, field: FieldDeclaration[V], commandContext: CommandContext): Option[V] = {
    val persistence = persistenceFor(entityName)
    val sourceUri = entityName.toUri(id)
    persistence.find(sourceUri).flatMap { entity =>
      field.toAdaptableField.sourceField(persistence.sourceType).findValue(entity, new CopyContext(sourceUri, commandContext))
    }
  }

  /** Find the field value for a certain entity by URI. */
  def find[V](uri: UriPath, field: FieldDeclaration[V], commandContext: CommandContext): Option[V] = {
    val entityName = UriPath.lastEntityNameOrFail(uri)
    UriPath.findId(uri, entityName).flatMap(find(entityName, _, field, commandContext))
  }

  /** Find a certain entity by URI and copy it to the targetType. */
  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T], commandContext: CommandContext): Option[T] = {
    persistenceFor(uri).find(uri, targetType, commandContext)
  }

  /** Find all a certain entity by URI and copy them to the targetType. */
  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T], commandContext: CommandContext): Seq[T] =
    persistenceFor(uri).findAll[T](uri, targetType, commandContext)

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef, commandContext: CommandContext): ID = {
    val sourceUri = entityName.toUri(idOpt)
    val persistence = persistenceFor(entityName)
    val dataToSave = entityTypeMap.entityType(entityName).copyAndUpdate(sourceType, source, sourceUri,
      persistence.targetType, persistence.newWritable(), commandContext)
    persistence.save(idOpt, dataToSave)
  }
}
