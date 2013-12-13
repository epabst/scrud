package com.github.scrud

import android.persistence.CursorField.PersistedId
import com.github.triangle._
import persistence.CrudPersistence
import platform.PlatformDriver
import platform.PlatformTypes._
import com.github.scrud.types.QualifiedType
import util.Common
import com.github.scrud.copy.{FieldApplicability, BaseAdaptableField, AdaptableField}
import scala.collection.mutable

/** An entity configuration that provides information needed to map data to and from persistence.
  * This shouldn't depend on the platform (e.g. android).
  * @author Eric Pabst (epabst@gmail.com)
  * @param entityName  this is used to identify the EntityType and for internationalized strings
  */
abstract class EntityType(val entityName: EntityName, val platformDriver: PlatformDriver) extends FieldList with Logging {
  override val logTag = Common.tryToEvaluate(entityName.name).getOrElse(Common.logTag)

  trace("Instantiated EntityType: " + this)

  val UriPathId = entityName.UriPathId

  /** This should only be used in order to override this.  IdField should be used instead of this.
    * A field that uses IdPk.id is NOT included here because it could match a related entity that also extends IdPk,
    * which results in many problems.
    */
  // not a val so that subclasses can use super.idField
  protected def idField: PortableField[ID] = UriPathId + PersistedId
  object IdField extends Field[ID](idField)

  /** The fields other than the primary key. */
  def valueFields: List[BaseField]

  /** The idField along with accessors for IdPk instances. */
  lazy val idPkField = IdField + Getter[IdPk,ID](_.id).withUpdater(e => e.withId(_)) +
    Setter((e: MutableIdPk) => e.id = _)
  lazy val fieldsIncludingIdPk = FieldList((idPkField +: fields): _*)

  /** These are all of the entity's fields, which includes IdPk.idField and the valueFields. */
  final lazy val fields: List[BaseField] = IdField +: valueFields

  lazy val parentFields: Seq[EntityField[_]] = EntityField.entityFields(this)

  lazy val parentEntityNames: Seq[EntityName] = parentFields.map(_.entityName)

  private val adaptableFields: mutable.Buffer[BaseAdaptableField] = mutable.Buffer[BaseAdaptableField]()
  
  protected def field[V](fieldName: String, qualifiedType: QualifiedType[V], applicability: FieldApplicability): AdaptableField[V] = {
    val newField = platformDriver.field(fieldName, qualifiedType, applicability, entityName)
    adaptableFields += newField
    newField
  }

  /** A PortableField for modifying a named portion of a View. */
  protected def namedViewField[T](fieldName: String, childViewField: PortableField[T]): PortableField[T] =
    platformDriver.namedViewField(fieldName, childViewField, entityName)

  /** A PortableField for modifying a named portion of a View. */
  protected def namedViewField[T](fieldName: String, qualifiedType: QualifiedType[T]): PortableField[T] =
    platformDriver.namedViewField(fieldName, qualifiedType, entityName)

  def toUri(id: ID) = UriPath(entityName, id)

  /**
   * Available to be overridden as needed by applications.
   * This is especially useful to create any initial data.
   * @param lowLevelPersistence The CrudPersistence as provided by [[com.github.scrud.platform.PlatformDriver.localDatabasePersistenceFactory]].
   *                            It is not wrapped by any other CrudPersistence so it may be matched as needed for additional access depending on the platform and implementation.
   */
  def onCreateDatabase(lowLevelPersistence: CrudPersistence) {}

  lazy val loadingValue: PortableValue = copyFrom(LoadingIndicator)

  override def toString = entityName.toString
}
