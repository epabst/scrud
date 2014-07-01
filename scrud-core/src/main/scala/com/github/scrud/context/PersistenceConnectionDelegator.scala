package com.github.scrud.context

import com.github.scrud._
import com.github.scrud.persistence.{CrudPersistence, PersistenceConnection}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.FieldDeclaration

/**
 * The context for a given interaction or command/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
//This is private so that subclasses CommandContext or CommandContextDelegator will be used instead.
trait PersistenceConnectionDelegator {
  def persistenceConnection: PersistenceConnection

  def persistenceFor(entityType: EntityType): CrudPersistence = persistenceConnection.persistenceFor(entityType)

  def persistenceFor(entityName: EntityName): CrudPersistence = persistenceFor(persistenceConnection.entityTypeMap.entityType(entityName))

  def persistenceFor(uri: UriPath): CrudPersistence = persistenceFor(UriPath.lastEntityNameOrFail(uri))

  /** Find the field value for a certain entity by ID. */
  def find[V](entityName: EntityName, id: ID, field: FieldDeclaration[V]): Option[V] = {
    val persistence = persistenceFor(entityName)
    val sourceUri = entityName.toUri(id)
    persistence.find(sourceUri).flatMap { entity =>
      field.toAdaptableField.sourceFieldOrFail(persistence.sourceType).findValue(entity, new CopyContext(sourceUri, persistenceConnection.commandContext))
    }
  }

  /** Find using this CommandContext's URI. */
  def findOrElseDefault[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] =
    find(uri, field).orElse(persistenceConnection.findDefault(field, uri))

  def findOrElseDefault[T <: AnyRef](uri: UriPath, targetType: TargetType, target: T): T = {
    val entityType = persistenceConnection.entityTypeMap.entityType(UriPath.lastEntityNameOrFail(uri))
    val persistence = persistenceFor(uri)
    persistence.find(uri).fold(persistenceConnection.findDefault(entityType, uri, targetType, target)) { source =>
      entityType.copyAndUpdate(persistence.sourceType, source, uri, targetType, target, persistenceConnection.commandContext)
    }
  }

  def findOrElseDefault(uri: UriPath, targetType: TargetType): AdaptedValueSeq = {
    val entityType = persistenceConnection.entityTypeMap.entityType(UriPath.lastEntityNameOrFail(uri))
    val persistence = persistenceFor(uri)
    persistence.find(uri).fold(persistenceConnection.findDefault(entityType, uri, targetType)) { source =>
      entityType.copy(persistence.sourceType, source, uri, targetType, persistenceConnection.commandContext)
    }
  }

  /** Find the non-empty field values for all entities matching a URI. */
  def findAll[V](uri: UriPath, field: FieldDeclaration[V]): Seq[V] = {
    persistenceFor(uri).findAll(uri, field, persistenceConnection.commandContext)
  }

  /** Find the field value for a certain entity by URI. */
  def find[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] = {
    val entityName = UriPath.lastEntityNameOrFail(uri)
    UriPath.findId(uri, entityName).flatMap(find(entityName, _, field))
  }

  /** Find a certain entity by URI and copy it to the targetType. */
  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Option[T] = {
    persistenceFor(uri).find(uri, targetType, persistenceConnection.commandContext)
  }

  /** Find all a certain entity by URI and copy them to the targetType. */
  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceFor(uri).findAll[T](uri, targetType, persistenceConnection.commandContext)

  /** Find all a certain entity by EntityName and copy them to the targetType. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    findAll(entityName.toUri, targetType)

  def delete(uri: UriPath): Int = persistenceFor(uri).delete(uri)

  def save(entityName: EntityName, idOpt: Option[ID], sourceType: SourceType, source: AnyRef): ID = {
    val sourceUri = entityName.toUri(idOpt)
    val persistence = persistenceFor(entityName)
    val dataToSave = persistenceConnection.entityTypeMap.entityType(entityName).copyAndUpdate(sourceType, source, sourceUri,
      persistence.writableType, persistence.newWritable(), persistenceConnection.commandContext)
    persistence.save(idOpt, dataToSave)
  }

  @deprecated("specify idOpt before sourceType", since = "2014-06-19")
  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID = {
    save(entityName, idOpt, sourceType, source)
  }
}
