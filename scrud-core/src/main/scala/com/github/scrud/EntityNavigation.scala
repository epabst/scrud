package com.github.scrud

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.context.ApplicationName
import com.github.scrud.action.{CrudOperationType, CrudOperation, StartEntityDeleteOperation, Action}

/**
 * The stateless definition of what navigation is available with respect to EntityTypes.
 * Default navigation is provided so simply instantiating one is sufficient.
 * However, when custom navigation is desired, override methods as needed.
 * Ideally each subclass won't assume the platform (e.g. android) so that it can be re-used for multiple platforms.
 * <p>No state should be stored in an instance.
 * Instead, put state into [[com.github.scrud.context.RequestContext]] and/or [[com.github.scrud.context.SharedContext]].
 * </p>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 9:01 AM
 */
class EntityNavigation(val applicationName: ApplicationName, val entityTypeMap: EntityTypeMap, val platformDriver: PlatformDriver) {

  /** The EntityType for the first page of the App. */
  val primaryEntityType: EntityType = entityTypeMap.allEntityTypes.head

  /**
   * Gets the actions that a user can perform from a given CrudOperation.
   * May be overridden to adjust the list of actions.
   */
  def actionsFromCrudOperation(crudOperation: CrudOperation): Seq[Action] = crudOperation match {
    case CrudOperation(entityName, CrudOperationType.Create) =>
//todo      parentEntityNames(entityName).flatMap(actionsToManage(_)) ++
          actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.Read) =>
//    todo  childEntityNames(entityName).flatMap(actionToList(_)) ++
          actionToUpdate(entityName).toSeq ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.List) =>
      actionToCreate(entityName).toSeq ++ actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName)
    case CrudOperation(entityName, CrudOperationType.Update) =>
      actionToDisplay(entityName).toSeq ++ //todo parentEntityNames(entityName).flatMap(actionsToManage(_)) ++
          actionToDelete(entityName).toSeq
  }

  protected def actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName: EntityName): Seq[Action] = {
//    val thisEntity = entityTypeMap.entityType(entityName)
//    thisEntity.parentFields match {
//      //exactly one parent w/o a display page
//      case parentField :: Nil if !actionToDisplay(parentField.entityName).isDefined => {
//        val parentEntityType = entityType(parentField.entityName)
//        //the parent's actionToUpdate should be shown since clicking on the parent entity brought the user
//        //to the list of child entities instead of to a display page for the parent entity.
//        actionToUpdate(parentEntityType).toSeq ++
//            childEntityTypes(parentEntityType).filter(_ != thisEntity).flatMap(actionToList(_))
//      }
//      case _ => 
        Nil
//    }
  }

  def actionsToManage(entityName: EntityName): Seq[Action] =
    actionToCreate(entityName).toSeq.flatMap(_ +: actionToList(entityName).toSeq)

  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityName: EntityName): Option[Action] =
    Some(Action(platformDriver.commandToListItems(entityName), platformDriver.operationToShowListUI(entityName)))

  /** Return true if the entity may be displayed in a mode that is distinct from editing. */
  protected def isDisplayableWithoutEditing(entityName: EntityName): Boolean = false

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityName: EntityName): Option[Action] = {
    if (isDisplayableWithoutEditing(entityName)) {
      Some(Action(platformDriver.commandToDisplayItem(entityName), platformDriver.operationToShowDisplayUI(entityName)))
    } else {
      None
    }
  }

  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionToCreate(entityName: EntityName): Option[Action] = {
    if (entityTypeMap.isCreatable(entityName))
      Some(Action(platformDriver.commandToAddItem(entityName), platformDriver.operationToShowCreateUI(entityName)))
    else
      None
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionToUpdate(entityName: EntityName): Option[Action] = {
    if (entityTypeMap.isSavable(entityName)) {
      Some(Action(platformDriver.commandToEditItem(entityName), platformDriver.operationToShowUpdateUI(entityName)))
    }
    else None
  }

  def actionToDelete(entityName: EntityName): Option[Action] = {
    if (entityTypeMap.isDeletable(entityName)) {
      Some(Action(platformDriver.commandToDeleteItem(entityName), StartEntityDeleteOperation(entityTypeMap.entityType(entityName))))
    } else {
      None
    }
  }
}
