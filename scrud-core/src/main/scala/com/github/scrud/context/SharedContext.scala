package com.github.scrud.context

import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.state.{StateHolder, State}
import com.github.scrud.util.ListenerHolder
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap, DataListener}
import com.github.scrud.platform.PlatformDriver

/**
 * The context that is shared among all [[com.github.scrud.context.RequestContext]]s.
 * Some examples are:<ul>
 *   <li>A Servlet Context</li>
 *   <li>A running Android Application</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait SharedContext extends StateHolder {
  def entityTypeMap: EntityTypeMap

  def platformDriver: PlatformDriver

  val applicationState: State = new State

  def dataListenerHolder(entityName: EntityName): ListenerHolder[DataListener] =
    dataListenerHolder(entityTypeMap.entityType(entityName))

  def dataListenerHolder(entityType: EntityType): ListenerHolder[DataListener] =
    entityTypeMap.persistenceFactory(entityType).listenerHolder(entityType, this)

  def withPersistence[T](f: PersistenceConnection => T): T = {
    val persistenceConnection = new PersistenceConnection(entityTypeMap, this)
    try f(persistenceConnection)
    finally persistenceConnection.close()
  }
}
