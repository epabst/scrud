package com.github.scrud.platform

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.action.Operation
import com.github.scrud.EntityType
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.util.{Logging, Name}
import java.net.URI
import scala.util.Try
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import com.github.scrud.action.PlatformCommand

/**
 * An API for an app to interact with the host platform such as Android.
 * It should be constructable without any kind of container.
 * It should not contain any application-specific information.
 * In fact, whether useful or not, the same instance should be shareable between two applications.
 * Use subtypes of CommandContext and SharedContext for state that is available in a container.
 * The model is that the custom platform is implemented for all applications by
 * delegating to the EntityNavigation and EntityTypes to make business logic decisions.
 * Then the EntityNavigation and EntityTyeps can call into this PlatformDriver or CommandContext
 * for any calls they need to make.
 * If direct access to the specific host platform is needed by a specific app, cast this
 * to the appropriate subclass, ideally using a scala match expression.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/25/12
 *         Time: 9:57 PM
 */
trait PlatformDriver extends Logging {
  /** This should only be used as a last resort.  Most logging should use ApplicationName's logging. */
  override protected val logTag: String = "scrud." + getClass.getSimpleName

  def tryBinaryResource(resourceName: Name): Try[URI]

  def localDatabasePersistenceFactory: PersistenceFactory

  def calculateDataVersion(entityTypes: Seq[EntityType]) =
    entityTypes.map(_.dataVersion).max

  /**
   * Gets the name of a field that contains the entity's ID.
   * @param entityName the entity whose id field is needed
   * @return the name of the field
   * @see [[com.github.scrud.EntityType.idField]]
   */
  def idFieldName(entityName: EntityName): String

  /** Get a localizable string key by name or fail. */
  def getStringKey(stringName: String): SKey = tryStringKey(stringName).get

  /** Try to get a localizable string key by name. */
  def tryStringKey(stringName: String): Try[SKey]

  /** Try to get a localizable image key by name. */
  def tryImageKey(imageName: Name): Try[ImgKey]

  def commandToListItems(entityName: EntityName): PlatformCommand

  def commandToDisplayItem(entityName: EntityName): PlatformCommand

  def commandToAddItem(entityName: EntityName): PlatformCommand

  def commandToEditItem(entityName: EntityName): PlatformCommand

  def commandToDeleteItem(entityName: EntityName): PlatformCommand

  /** The command to undo the last delete. */
  def commandToUndoDelete: PlatformCommand

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName): Operation

  def platformSpecificFieldFactories: Seq[AdaptableFieldFactory]

  private object AdaptableFieldConvertibleFactory extends AdaptableFieldFactory {
    def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]) = {
      val convertibles = representations.collect {
        case convertible: AdaptableFieldConvertible[V] => convertible
      }
      val field = AdaptableField(convertibles.map(_.toAdaptableField))
      AdaptableFieldWithRepresentations(field, convertibles.toSet)
    }
  }

  final lazy val fieldFactories = platformSpecificFieldFactories ++
    Seq(AdaptableFieldConvertibleFactory, MapStorageAdaptableFieldFactory)

  def field[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): ExtensibleAdaptableField[V] = {
    val fieldWithRepresentations = adapt(fieldFactories, entityName, fieldName, qualifiedType, representations)
    fieldWithRepresentations.field
  }

  protected def adapt[V](fieldFactories: Seq[AdaptableFieldFactory], entityName: EntityName, fieldName: FieldName,
                         qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    fieldFactories.headOption match {
      case Some(fieldFactory) =>
        val adaptableFieldWithRepresentations = fieldFactory.adapt(entityName, fieldName, qualifiedType, representations)
        val unusedRepresentations = representations.filterNot(adaptableFieldWithRepresentations.representations.contains(_))
        val tailField = adapt(fieldFactories.tail, entityName, fieldName, qualifiedType, unusedRepresentations)
        adaptableFieldWithRepresentations.orElse(tailField)
      case None =>
        adaptUnusedRepresentations(entityName, fieldName, qualifiedType, representations)
    }
  }

  protected def adaptUnusedRepresentations[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], unusedRepresentations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    if (!unusedRepresentations.isEmpty) {
      info("Representations that were not used: " + unusedRepresentations.mkString(", "))
    }
    AdaptableFieldWithRepresentations.empty
  }
}
