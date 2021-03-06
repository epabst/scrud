package com.github.scrud

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.context.ApplicationName
import com.github.scrud.action._
import CrudOperationType._
import com.github.scrud.action.OperationAction
import com.github.scrud.action.StartEntityDeleteOperation
import com.github.scrud.action.CrudOperation

/**
 * The stateless definition of what navigation is available with respect to EntityTypes.
 * Default navigation is provided so simply instantiating one is sufficient.
 * However, when custom navigation is desired, override methods as needed.
 * Ideally each subclass won't assume the platform (e.g. android) so that it can be re-used for multiple platforms.
 * <p>No state should be stored in an instance.
 * Instead, put state into [[com.github.scrud.context.CommandContext]] and/or [[com.github.scrud.context.SharedContext]].
 * </p>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 9:01 AM
 */
class EntityNavigation(val entityTypeMap: EntityTypeMap) {
  def applicationName: ApplicationName = entityTypeMap.applicationName

  def platformDriver: PlatformDriver = entityTypeMap.platformDriver

  /** The EntityType for the first page of the App. */
  val primaryEntityType: EntityType = entityTypeMap.allEntityTypes.head

  /**
   * Gets the actions that a user can perform from a given CrudOperation.
   * May be overridden to adjust the list of actions.
   */
  def actionsFromCrudOperation(crudOperation: CrudOperation): Seq[OperationAction] = crudOperation match {
    case CrudOperation(entityName, Create) =>
      entityTypeMap.upstreamEntityNames(entityName).flatMap(actionsToManageList(_)) ++ actionsToDelete(entityName)
    case CrudOperation(entityName, Read) =>
      entityTypeMap.downstreamEntityNames(entityName).flatMap(actionsToList(_)) ++
          actionsToUpdate(entityName) ++ actionsToDelete(entityName)
    case CrudOperation(entityName, List) =>
      actionsToCreate(entityName) ++ actionsToUpdateAndListDownstreamsOfOnlyUpstreamWithoutDisplayAction(entityName)
    case CrudOperation(entityName, Update) =>
      actionsToDisplay(entityName) ++ entityTypeMap.upstreamEntityNames(entityName).flatMap(actionsToManageList(_)) ++
          actionsToDelete(entityName)
  }

  protected def actionsToUpdateAndListDownstreamsOfOnlyUpstreamWithoutDisplayAction(entityName: EntityName): Seq[OperationAction] = {
//    val thisEntity = entityTypeMap.entityType(entityName)
//    thisEntity.upstreamFields match {
//      //exactly one upstream w/o a display page
//      case upstreamField :: Nil if !actionToDisplay(upstreamField.entityName).isDefined => {
//        val upstreamEntityType = entityType(upstreamField.entityName)
//        //the upstream's actionToUpdate should be shown since clicking on the upstream entity brought the user
//        //to the list of downstream entities instead of to a display page for the upstream entity.
//        actionsToUpdate(upstreamEntityType) ++
//            downstreamEntityTypes(upstreamEntityType).filter(_ != thisEntity).flatMap(actionToList(_))
//      }
//      case _ => 
        Nil
//    }
  }

  def actionsToManageList(entityName: EntityName): Seq[OperationAction] = {
    val createActions = actionsToCreate(entityName)
    if (!createActions.isEmpty) {
      createActions ++ actionsToList(entityName)
    } else {
      Nil
    }
  }

  /** Gets the action(s) to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionsToList(entityName: EntityName): Seq[OperationAction] =
    Seq(OperationAction(platformDriver.commandToListItems(entityName), platformDriver.operationToShowListUI(entityName)))

  /** Return true if the entity may be displayed in a mode that is distinct from editing. */
  protected def isDisplayableWithoutEditing(entityName: EntityName): Boolean = false

  /** Gets the actions to display the entity given the id in the UriPath. */
  def actionsToDisplay(entityName: EntityName): Seq[OperationAction] = {
    if (isDisplayableWithoutEditing(entityName)) {
      Seq(OperationAction(platformDriver.commandToDisplayItem(entityName), platformDriver.operationToShowDisplayUI(entityName)))
    } else {
      Nil
    }
  }

  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionsToCreate(entityName: EntityName): Seq[OperationAction] = {
    if (entityTypeMap.isCreatable(entityName)) {
      Seq(OperationAction(platformDriver.commandToAddItem(entityName), platformDriver.operationToShowCreateUI(entityName)))
    } else {
      Nil
    }
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionsToUpdate(entityName: EntityName): Seq[OperationAction] = {
    if (entityTypeMap.isSavable(entityName)) {
      Seq(OperationAction(platformDriver.commandToEditItem(entityName), platformDriver.operationToShowUpdateUI(entityName)))
    } else {
      Nil
    }
  }

  def actionsToDelete(entityName: EntityName): Seq[OperationAction] = {
    if (entityTypeMap.isDeletable(entityName)) {
      Seq(OperationAction(platformDriver.commandToDeleteItem(entityName), StartEntityDeleteOperation(entityTypeMap.entityType(entityName))))
    } else {
      Nil
    }
  }
}
