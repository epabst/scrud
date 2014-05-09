package com.github.scrud.context

import com.github.scrud.{EntityNavigation, FieldDeclaration, UriPath, EntityName}
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.action.Undoable
import com.github.scrud.copy.SourceType

/**
 * The context for a given interaction or command/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
private[context] trait CommandContextHolder extends SharedContextHolder {
  protected def commandContext: CommandContext

  def entityNavigation: EntityNavigation

  def persistenceConnection: PersistenceConnection

  def findDefault[V](field: FieldDeclaration[V], sourceUri: UriPath): Option[V] =
    field.toAdaptableField.findDefault(sourceUri, commandContext)

  /** Find using this CommandContext's URI. */
  def find[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] =
    persistenceConnection.find(uri, field, commandContext).orElse(findDefault(field, uri))

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID =
    persistenceConnection.save(entityName, sourceType, idOpt, source, commandContext)

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
