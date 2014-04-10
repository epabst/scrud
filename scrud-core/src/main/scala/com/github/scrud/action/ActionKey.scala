package com.github.scrud.action

import com.github.scrud.util.Name
import com.github.scrud.action.RestMethod.RestMethod
import com.github.scrud.platform.representation.EditUI

/**
 * The unique identifier for an Action.
 * The intent may depend heavily on the Uri it is used with.
 * The string is not intended for display.  It should be localized to a display string.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/20/12
 * Time: 6:20 PM
 * @param name a name such as a Java identifier like PoliceOfficer (pascal case) or crescentWrench (camel case).
 * @param restMethodOpt which REST method correlates to the action, if any.
 * @param actionDataTypeOpt which [[com.github.scrud.action.ActionDataType]] is required for the action, if any.
 *                          If one is specified, the application should first gather data from the user for fields with the ActionDataType.
 * @see [[com.github.scrud.action.Action]]
 */
case class ActionKey(name: String, restMethodOpt: Option[RestMethod] = None,
                     actionDataTypeOpt: Option[ActionDataType] = None) extends Name

/**
 * This contains a set of common [[com.github.scrud.action.ActionKey]]s.
 * Others may be created as needed by specific applications.
 */
object ActionKey {
  /**
   * Create/save a new entity after allowing the user to fill in data.  The new Uri will be returned.
   * Normally, the UI will render this as an "Add" button or a "+" button, transitioning to a "Create" or "Save" button.
   * This represents part of the "C" in CRUD.
   * This corresponds to a POST in REST.
   */
  object Create extends ActionKey("Create", Some(RestMethod.POST), Some(EditUI))

  /**
   * The command to get data for a Uri, whether a single entity or a list of entities.
   * Normally, the UI will render this as clickable text.
   * This represents the "R" in CRUD.
   * This corresponds to a GET in REST.
   */
  object View extends ActionKey("View", Some(RestMethod.GET))

  /**
   * The command to save an entity with a given Uri after allowing the user to modify the data.
   * Normally, the UI will render this is an "Edit" button or a pencil icon, transitioning to a "Save" button.
   * This represents part of the "U" in CRUD.
   * This corresponds to a PUT in REST.
   */
  object Update extends ActionKey("Update", Some(RestMethod.PUT), Some(EditUI))

  /**
   * The command to delete an entity.
   * Normally, the UI will render this is a "Delete" button or red "X" icon.
   * This represents the "D" in CRUD.
   * This corresponds to a DELETE in REST.
   */
  object Delete extends ActionKey("Delete", Some(RestMethod.DELETE))
}
