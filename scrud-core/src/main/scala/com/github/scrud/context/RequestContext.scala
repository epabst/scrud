package com.github.scrud.context

import com.github.scrud.UriPath
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap}
import com.github.scrud.action.CrudOperationType.CrudOperationType
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityName
import com.github.scrud.action.Undoable
import com.github.scrud.copy.{InstantiatingTargetType, SourceType}

/**
 * The context for a given interaction or request/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * It should have an action and a [[com.github.scrud.UriPath]],
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait RequestContext {
  def operationType: CrudOperationType

  def uri: UriPath

  def sharedContext: SharedContext

  def entityTypeMap: EntityTypeMap = sharedContext.entityTypeMap

  def platformDriver: PlatformDriver = sharedContext.platformDriver

  lazy val persistenceConnection: PersistenceConnection = {
    val persistenceConnection = sharedContext.openPersistence()
    state.addListener(new DestroyStateListener {
      override def onDestroyState() {
        persistenceConnection.close()
      }
    })
    persistenceConnection
  }

  def findAll(entityName: EntityName): Seq[AnyRef] = persistenceConnection.persistenceFor(entityName).findAll(uri)

  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceConnection.persistenceFor(entityName).findAll[T](uri, targetType, this)

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID = {
    val persistence = persistenceConnection.persistenceFor(entityName)
    val dataToSave = entityTypeMap.entityType(entityName).copyAndUpdate(sourceType, source, persistence.targetType, persistence.newWritable(), this)
    persistence.save(idOpt, dataToSave)
  }

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private[context] val state: State = new State

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
