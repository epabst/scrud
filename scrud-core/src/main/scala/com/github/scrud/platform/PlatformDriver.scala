package com.github.scrud.platform

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.action.Operation
import com.github.scrud.EntityType
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.action.PlatformCommand
import com.github.scrud.util.{Name, Logging}
import com.github.scrud.platform.representation.PersistenceRange
import com.netaporter.uri.Uri
import scala.util.Try

/**
 * An API for an app to interact with the host platform such as Android.
 * It should be constructable without any kind of container.
 * Use subtypes of RequestContext for state that is available in a container.
 * The model is that the custom platform is implemented for all applications by
 * delegating to the CrudApplication to make business logic decisions.
 * Then the CrudApplication can call into this PlatformDriver or RequestContext for any calls it needs to make.
 * If direct access to the specific host platform is needed by a specific app, cast this
 * to the appropriate subclass, ideally using a scala match expression.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/25/12
 *         Time: 9:57 PM
 */
trait PlatformDriver extends Logging {
  def tryResource(resourceName: Name): Try[Uri]
  
  def localDatabasePersistenceFactory: PersistenceFactory

  def calculateDataVersion(entityTypes: Seq[EntityType]) = {
    (for {
      entityType <- entityTypes
      fieldDeclaration <- entityType.fieldDeclarations
      persistenceRange <- fieldDeclaration.representations.collect {
        case persistenceRange: PersistenceRange => persistenceRange
      }
    } yield {
      if (persistenceRange.maxDataVersion < Int.MaxValue) {
        persistenceRange.maxDataVersion + 1
      } else {
        persistenceRange.minDataVersion
      }
    }).max
  }

  /**
   * Gets the name of a field that contains the entity's ID.
   * @param entityName the entity whose id field is needed
   * @return the name of the field
   * @see [[com.github.scrud.EntityType.idField]]
   */
  def idFieldName(entityName: EntityName): String

  def commandToListItems(entityName: EntityName): PlatformCommand

  def commandToDisplayItem(entityName: EntityName): PlatformCommand

  def commandToAddItem(entityName: EntityName): PlatformCommand

  def commandToEditItem(entityName: EntityName): PlatformCommand

  def commandToDeleteItem(entityName: EntityName): PlatformCommand

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName): Operation

  /** The command to undo the last delete. */
  def commandToUndoDelete: PlatformCommand

  def platformSpecificFieldFactories: Seq[AdaptableFieldFactory]

  private object AdaptableFieldConvertibleFactory extends AdaptableFieldFactory {
    def adapt[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]) = {
      val convertibles = representations.collect {
        case convertible: AdaptableFieldConvertible[V] => convertible
      }
      val field = CompositeAdaptableField(convertibles.map(_.toAdaptableField))
      AdaptableFieldWithRepresentations(field, convertibles.toSet)
    }
  }

  final lazy val fieldFactories = platformSpecificFieldFactories :+ AdaptableFieldConvertibleFactory

  def field[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): ExtensibleAdaptableField[V] = {
    val fieldWithRepresentations = adapt(fieldFactories, entityName, fieldName, qualifiedType, representations)
    fieldWithRepresentations.field
  }

  protected def adapt[V](fieldFactories: Seq[AdaptableFieldFactory], entityName: EntityName, fieldName: String,
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

  protected def adaptUnusedRepresentations[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], unusedRepresentations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    if (!unusedRepresentations.isEmpty) {
      info("Representations that were not used: " + unusedRepresentations.mkString(", "))
    }
    AdaptableFieldWithRepresentations.empty
  }
}
