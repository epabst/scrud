package com.github.scrud.platform

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.action.Operation
import com.github.scrud.EntityType
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.action.Command
import com.github.scrud.util.Logging

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
  def localDatabasePersistenceFactory: PersistenceFactory

  def calculateDataVersion(entityTypes: Seq[EntityType]): Int

  /**
   * Gets the name of a field that contains an entity ID.
   * @param entityName the entity whose id field is needed
   * @param primaryKey true if the primary key field for the entity, false if referenced from another entity
   * @return the name of the field
   * @see [[com.github.scrud.EntityType.idField]]
   */
  def idFieldName(entityName: EntityName, primaryKey: Boolean = true): String

  def commandToListItems(entityName: EntityName): Command

  def commandToDisplayItem(entityName: EntityName): Command

  def commandToAddItem(entityName: EntityName): Command

  def commandToEditItem(entityName: EntityName): Command

  def commandToDeleteItem(entityName: EntityName): Command

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName): Operation

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName): Operation

  /** The command to undo the last delete. */
  def commandToUndoDelete: Command

  def fieldFactories: Seq[AdaptableFieldFactory]

  private lazy val fieldFactoriesVal = fieldFactories

  def field[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): ExtensibleAdaptableField[V] = {
    val fieldWithRepresentations = adapt(fieldFactoriesVal, entityName, fieldName, qualifiedType, representations)
    fieldWithRepresentations.field
  }

  protected def adapt[V](fieldFactories: Seq[AdaptableFieldFactory], entityName: EntityName, fieldName: String,
                         qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    fieldFactories.headOption match {
      case Some(fieldFactory) =>
        val adaptableFieldWithRepresentations = fieldFactory.adapt(entityName, fieldName, qualifiedType, representations)
        val unusedRepresentations = representations.filterNot(adaptableFieldWithRepresentations.representations.contains(_))
        adapt(fieldFactories.tail, entityName, fieldName, qualifiedType, unusedRepresentations)
      case None =>
        adaptUnusedRepresentations(entityName, fieldName, qualifiedType, representations)
    }
  }

  protected def adaptUnusedRepresentations[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], unusedRepresentations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    info("Representations that were not used: " + unusedRepresentations.mkString(", "))
    AdaptableFieldWithRepresentations.empty
  }
}
