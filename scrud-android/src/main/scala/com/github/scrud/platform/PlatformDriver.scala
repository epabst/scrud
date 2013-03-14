package com.github.scrud.platform

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.action.{Command, Operation}
import com.github.scrud.EntityName
import com.github.scrud.types.QualifiedType
import com.github.triangle.PortableField

/**
 * An API for an app to interact with the host platform such as Android.
 * It should be constructable without any kind of container.
 * Use subtypes of CrudContext for state that is available in a container.
 * The model is that the custom platform is implemented for all applications by
 * delegating to the CrudApplication to make business logic decisions.
 * Then the CrudApplication can call into this PlatformDriver or CrudContext for any calls it needs to make.
 * If direct access to the specific host platform is needed by a specific app, cast this
 * to the appropriate subclass, ideally using a scala match expression.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/25/12
 *         Time: 9:57 PM
 */
trait PlatformDriver {
  def localDatabasePersistenceFactory: PersistenceFactory

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

  /** A PortableField for modifying a named portion of a View. */
  def namedViewField[T](fieldName: String, childViewField: PortableField[T], entityName: EntityName): PortableField[T]

  /**
   * A PortableField for modifying a named portion of a View.
   * The platform is expected to recognize the qualifiedType and be able to return a PortableField.
   * @throws MatchError if the qualifiedType is not recognized.
   */
  def namedViewField[T](fieldName: String, qualifiedType: QualifiedType[T], entityName: EntityName): PortableField[T]
}
