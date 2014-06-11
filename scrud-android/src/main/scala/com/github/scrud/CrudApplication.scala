package com.github.scrud

import persistence.EntityTypeMap
import platform.PlatformDriver
import com.github.scrud.util.{ExternalLogging, DelegateLogging}

/**
 * A stateless Application that uses Scrud.  It has all the configuration for how the application behaves,
 * but none of its actual state.
 * It that works with pairings of an [[com.github.scrud.EntityType]] and
 * a [[com.github.scrud.persistence.PersistenceFactory]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */
@deprecated("use EntityNavigation or EntityTypeMap", since = "2014-05-12")
abstract class CrudApplication(val platformDriver: PlatformDriver, val entityTypeMap: EntityTypeMap) extends DelegateLogging {
  override protected def loggingDelegate: ExternalLogging = entityTypeMap.applicationName

  final def name: String = entityTypeMap.applicationName.name

  lazy val entityNavigation: EntityNavigation = new EntityNavigation(entityTypeMap)

  val packageName: String = getClass.getPackage.getName

  /** The EntityType for the first page of the App. */
  @deprecated("use entityNavigation.primaryEntityType", since = "2014-05-28")
  lazy val primaryEntityType: EntityType = entityNavigation.primaryEntityType

  @deprecated("use entityTypeMap.upstreamEntityNames(entityName)", since = "2014-05-28")
  def parentEntityNames(entityName: EntityName): Seq[EntityName] = entityTypeMap.upstreamEntityNames(entityName)

  @deprecated("use entityTypeMap.downstreamEntityNames(entityType)", since = "2014-05-28")
  def childEntityNames(entityName: EntityName): Seq[EntityName] =
    entityTypeMap.downstreamEntityNames(entityName)

  @deprecated("use entityTypeMap.downstreamEntityTypes(entityType)", since = "2014-05-28")
  def childEntityTypes(entityType: EntityType): Seq[EntityType] =
    entityTypeMap.downstreamEntityTypes(entityType)

}
