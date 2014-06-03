package com.github.scrud.context

import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.state.{StateHolder, State}
import com.github.scrud.util.{DelegateLogging, ListenerHolder}
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap, DataListener}
import com.github.scrud.platform.PlatformDriver

/**
 * The context that is shared among all [[com.github.scrud.context.CommandContext]]s
 * for a single application (as identified by an [[com.github.scrud.context.ApplicationName]]).
 * Some examples are:<ul>
 *   <li>A Servlet Context</li>
 *   <li>A running Android Application</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait SharedContext extends StateHolder with DelegateLogging {
  def entityTypeMap: EntityTypeMap

  def applicationName: ApplicationName = entityTypeMap.applicationName

  def platformDriver: PlatformDriver = entityTypeMap.platformDriver

  val applicationState: State = new State

  lazy val asStubCommandContext: StubCommandContext = new StubCommandContext(this)

  protected def loggingDelegate = applicationName

  def dataListenerHolder(entityName: EntityName): ListenerHolder[DataListener] =
    dataListenerHolder(entityTypeMap.entityType(entityName))

  def dataListenerHolder(entityType: EntityType): ListenerHolder[DataListener] =
    entityTypeMap.persistenceFactory(entityType).listenerHolder(entityType, this)

  def withPersistence[T](f: PersistenceConnection => T): T = {
    val persistenceConnection = openPersistence()
    try f(persistenceConnection)
    finally persistenceConnection.close()
  }

  def openPersistence(): PersistenceConnection = {
    new PersistenceConnection(entityTypeMap, this)
  }
}
