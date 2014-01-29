package com.github.scrud

import persistence.CrudPersistence
import platform.PlatformDriver
import platform.PlatformTypes._
import com.github.scrud.types.QualifiedType
import com.github.scrud.util.{Logging, Common}
import com.github.scrud.copy._
import scala.collection.mutable
import com.github.scrud.copy.FieldApplicability

/** An entity configuration that provides information needed to map data to and from persistence.
  * This shouldn't depend on the platform (e.g. android).
  * @author Eric Pabst (epabst@gmail.com)
  * @param entityName  this is used to identify the EntityType and for internationalized strings
  */
abstract class EntityType(val entityName: EntityName, val platformDriver: PlatformDriver) extends Logging {
  override val logTag = Common.tryToEvaluate(entityName.name).getOrElse(Common.logTag)

  trace("Instantiated EntityType: " + this)

  private val adaptableFields: mutable.Buffer[BaseAdaptableField] = mutable.Buffer[BaseAdaptableField]()

  def idFieldName: String = platformDriver.idFieldName(entityName)

  def findPersistedId(readable: AnyRef): Option[ID] //todo = platformDriver...

  protected def field[V](fieldName: String, qualifiedType: QualifiedType[V], applicability: FieldApplicability): AdaptableField[V] = {
    val newField = platformDriver.field(fieldName, qualifiedType, applicability, entityName)
    adaptableFields += newField
    newField
  }

  def clearId(source: IdPk): IdPk = source.withId(None)

  def clearId(source: AnyRef): AnyRef = new UnsupportedOperationException

  def copyAndUpdate[T <: AnyRef](sourceType: SourceType, source: AnyRef, targetType: InstantiatingTargetType[T]): T

  def toUri(id: ID) = UriPath(entityName, id)

  /**
   * Available to be overridden as needed by applications.
   * This is especially useful to create any initial data.
   * @param lowLevelPersistence The CrudPersistence as provided by [[com.github.scrud.platform.PlatformDriver.localDatabasePersistenceFactory]].
   *                            It is not wrapped by any other CrudPersistence so it may be matched as needed for additional access depending on the platform and implementation.
   */
  def onCreateDatabase(lowLevelPersistence: CrudPersistence) {}

  override def toString = entityName.toString
}
