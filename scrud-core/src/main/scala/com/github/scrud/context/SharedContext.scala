package com.github.scrud.context

import com.github.scrud.{EntityType, EntityName}
import com.github.scrud.state.{StateHolder, State}
import com.github.scrud.util.ListenerHolder
import com.github.scrud.persistence.{CrudPersistence, EntityTypeMap, DataListener}
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

  def openEntityPersistence(entityName: EntityName): CrudPersistence =
    openEntityPersistence(entityTypeMap.entityType(entityName))

  def openEntityPersistence(entityType: EntityType): CrudPersistence =
    entityTypeMap.persistenceFactory(entityType).createEntityPersistence(entityType, this)

  def withEntityPersistence[T](entityName: EntityName)(f: CrudPersistence => T): T = {
    withEntityPersistence(entityTypeMap.entityType(entityName))(f)
  }

  def withEntityPersistence[T](entityType: EntityType)(f: CrudPersistence => T): T = {
    val persistence = openEntityPersistence(entityType)
    try f(persistence)
    finally persistence.close()
  }
}
