package com.github.scrud

import _root_.android.R
import action._
import action.OperationAction
import action.Command
import action.CommandId
import action.CrudOperation
import action.StartEntityDeleteOperation
import persistence.{PersistenceFactoryMapping, PersistenceFactory}
import platform.PlatformDriver
import platform.PlatformTypes._
import state.ApplicationConcurrentMapVal
import com.github.scrud.util.{Logging, Common, UrgentFutureExecutor}
import java.util.NoSuchElementException
import com.github.scrud
import scrud.android.{CrudType,NamingConventions}
import scrud.android.view.AndroidResourceAnalyzer._

/**
 * A stateless Application that uses Scrud.  It has all the configuration for how the application behaves,
 * but none of its actual state.
 * It that works with pairings of an [[com.github.scrud.EntityType]] and
 * a [[com.github.scrud.persistence.PersistenceFactory]].
 * Internally it uses [[com.github.scrud.android.CrudType]]s.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */
@deprecated("use EntityNavigation or EntityTypeMap")
abstract class CrudApplication(val platformDriver: PlatformDriver) extends PersistenceFactoryMapping with Logging {
  lazy val logTag = Common.tryToEvaluate(nameId).getOrElse(Common.logTag)

  def name: String

  //this will be used for programmatic uses such as a database name
  lazy val nameId: String = name.replace(" ", "_").toLowerCase

  val packageName: String = getClass.getPackage.getName

  /** All entities in the application, in order as shown to users. */
  protected def allCrudTypes: Seq[CrudType]
  def allEntityTypes: Seq[EntityType] = allCrudTypes.map(_.entityType)

  /** The EntityType for the first page of the App. */
  lazy val primaryEntityType: EntityType = allEntityTypes.head

  def parentEntityNames(entityName: EntityName): Seq[EntityName] = entityType(entityName).parentEntityNames

  def childEntityNames(entityName: EntityName): Seq[EntityName] = childEntityTypes(entityType(entityName)).map(_.entityName)

  def childEntityTypes(entityType: EntityType): Seq[EntityType] = {
    trace("childEntities: allCrudTypes=" + allEntityTypes + " self=" + entityType)
    allEntityTypes.filter { nextEntity =>
      val parentEntityNames = nextEntity.parentEntityNames
      trace("childEntities: parents of " + nextEntity + " are " + parentEntityNames)
      parentEntityNames.contains(entityType.entityName)
    }
  }

  private def crudType(entityName: EntityName): CrudType =
    allCrudTypes.find(_.entityType.entityName == entityName).getOrElse(throw new NoSuchElementException(entityName + " not found"))

  def persistenceFactory(entityType: EntityType): PersistenceFactory = crudType(entityType.entityName).persistenceFactory

  private lazy val classInApplicationPackage: Class[_] = allEntityTypes.head.getClass
  lazy val rStringClasses: Seq[Class[_]] = detectRStringClasses(classInApplicationPackage)
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(classInApplicationPackage)
  lazy val rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)

  protected def getStringKey(stringName: String): SKey =
    findResourceIdWithName(rStringClasses, stringName).getOrElse {
      rStringClasses.foreach(rStringClass => logError("Contents of " + rStringClass + " are " + rStringClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.string." + stringName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
              rStringClasses.mkString("(string classes: ", ",", ")"))
    }

  def entityNameLayoutPrefixFor(entityName: EntityName) = NamingConventions.toLayoutPrefix(entityName)

  def commandToListItems(entityName: EntityName): Command = Command(CommandId(entityNameLayoutPrefixFor(entityName) + "_list"), None,
    findResourceIdWithName(rStringClasses, entityNameLayoutPrefixFor(entityName) + "_list"))

  def commandToDisplayItem(entityName: EntityName): Command = Command(CommandId("display_" + entityNameLayoutPrefixFor(entityName)),
    None, None)

  def commandToAddItem(entityName: EntityName): Command = Command(CommandId("add_" + entityNameLayoutPrefixFor(entityName)),
    Some(R.drawable.ic_menu_add),
    Some(getStringKey("add_" + entityNameLayoutPrefixFor(entityName))))

  def commandToEditItem(entityName: EntityName): Command = Command(CommandId("edit_" + entityNameLayoutPrefixFor(entityName)),
    Some(R.drawable.ic_menu_edit), Some(getStringKey("edit_" + entityNameLayoutPrefixFor(entityName))))

  def commandToDeleteItem(entityName: EntityName): Command = Command(CommandId("delete_" + entityNameLayoutPrefixFor(entityName)),
    Some(R.drawable.ic_menu_delete), Some(res.R.string.delete_item))

  def displayLayoutFor(entityName: EntityName): Option[LayoutKey] = findResourceIdWithName(rLayoutClasses, entityNameLayoutPrefixFor(entityName) + "_display")
  def hasDisplayPage(entityName: EntityName) = displayLayoutFor(entityName).isDefined

  /**
   * Gets the actions that a user can perform from a given CrudOperation.
   * May be overridden to modify the list of actions.
   */
  def actionsFromCrudOperation(crudOperation: CrudOperation): Seq[OperationAction] = (crudOperation match {
    case CrudOperation(entityName, CrudOperationType.Create) =>
      parentEntityNames(entityName).flatMap(actionsToManage(_)) ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.Read) =>
      childEntityNames(entityName).flatMap(actionToList(_)) ++
          actionToUpdate(entityName).toSeq ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.List) =>
      actionToCreate(entityName).toSeq ++ actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName)
    case CrudOperation(entityName, CrudOperationType.Update) =>
      actionToDisplay(entityName).toSeq ++ parentEntityNames(entityName).flatMap(actionsToManage(_)) ++ actionToDelete(entityName).toSeq
  })

  protected def actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName: EntityName): Seq[OperationAction] = {
    val thisEntity = entityType(entityName)
    (thisEntity.parentFields match {
      //exactly one parent w/o a display page
      case parentField :: Nil if !actionToDisplay(parentField.entityName).isDefined => {
        val parentEntityType = entityType(parentField.entityName)
        //the parent's actionToUpdate should be shown since clicking on the parent entity brought the user
        //to the list of child entities instead of to a display page for the parent entity.
        actionToUpdate(parentEntityType).toSeq ++
          childEntityTypes(parentEntityType).filter(_ != thisEntity).flatMap(actionToList(_))
      }
      case _ => Nil
    })
  }

  def actionsToManage(entityName: EntityName): Seq[OperationAction] =
    actionToCreate(entityName).toSeq.flatMap(_ +: actionToList(entityName).toSeq)

  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionToCreate(entityType: EntityType): Option[OperationAction] = actionToCreate(entityType.entityName)

  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionToCreate(entityName: EntityName): Option[OperationAction] = {
    if (isCreatable(entityName))
      Some(OperationAction(commandToAddItem(entityName), platformDriver.operationToShowCreateUI(entityName)))
    else
      None
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionToUpdate(entityType: EntityType): Option[OperationAction] = actionToUpdate(entityType.entityName)
  def actionToUpdate(entityName: EntityName): Option[OperationAction] = {
    if (isSavable(entityName)) Some(OperationAction(commandToEditItem(entityName), platformDriver.operationToShowUpdateUI(entityName)))
    else None
  }

  def actionToDelete(entityName: EntityName): Option[OperationAction] = actionToDelete(entityType(entityName))
  def actionToDelete(entityType: EntityType): Option[OperationAction] = {
    if (isDeletable(entityType)) {
      Some(OperationAction(commandToDeleteItem(entityType.entityName), StartEntityDeleteOperation(entityType)))
    } else None
  }

  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityType: EntityType): Option[OperationAction] = actionToList(entityType.entityName)
  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityName: EntityName): Option[OperationAction] =
    Some(OperationAction(commandToListItems(entityName), platformDriver.operationToShowListUI(entityName)))

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityType: EntityType): Option[OperationAction] = actionToDisplay(entityType.entityName)

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityName: EntityName): Option[OperationAction] = {
    if (hasDisplayPage(entityName)) {
      Some(OperationAction(commandToDisplayItem(entityName), platformDriver.operationToShowDisplayUI(entityName)))
    } else None
  }

  private[scrud] object FuturePortableValueCache
    extends ApplicationConcurrentMapVal[(EntityType, UriPath, CrudContext),Future[PortableValue]]

  /**
   * Save the data into the persistence for entityType.
   * If data is invalid (based on updating a ValidationResult), returns None, otherwise returns the created or updated ID.
   */
  def saveIfValid(data: AnyRef, entityType: EntityType, requestContext: RequestContext): Option[ID] = {
    val crudContext = requestContext.crudContext
    val updaterInput = UpdaterInput(crudContext.newWritable(entityType), requestContext)
    val relevantFields = entityType.copyableTo(updaterInput)
    val portableValue = relevantFields.copyFrom(data +: requestContext)
    if (portableValue.update(ValidationResult.Valid).isValid) {
      val updatedWritable = portableValue.update(updaterInput)
      val idOpt = entityType.IdField(requestContext.currentUriPath)
      val newId = crudContext.withEntityPersistence(entityType)(_.save(idOpt, updatedWritable))
      debug("Saved " + portableValue + " into id=" + newId + " entityType=" + entityType)
      crudContext.displayMessageToUserBriefly(res.R.string.data_saved_notification)
      Some(newId)
    } else {
      crudContext.displayMessageToUserBriefly(res.R.string.data_not_saved_since_invalid_notification)
      None
    }
  }

  private lazy val executor = new UrgentFutureExecutor()

  private def cachedFuturePortableValueOrCalculate(entityType: EntityType, uriPathWithId: UriPath, crudContext: CrudContext)(calculate: => PortableValue): Future[PortableValue] = {
    val cache = FuturePortableValueCache.get(crudContext.stateHolder)
    val key = (entityType, uriPathWithId, crudContext)
    cache.get(key).getOrElse {
      val futurePortableValue = executor.urgentFuture {
        crudContext.withExceptionReportingHavingDefaultReturnValue(PortableValue.nothing) {
          calculate
        }
      }
      cache.putIfAbsent(key, futurePortableValue).getOrElse(futurePortableValue)
    }
  }

  def futurePortableValue(entityType: EntityType, uriPathWithId: UriPath, crudContext: CrudContext): Future[PortableValue] = {
    cachedFuturePortableValueOrCalculate(entityType, uriPathWithId, crudContext) {
      calculatePortableValue(entityType, uriPathWithId, crudContext)
    }
  }

  def futurePortableValue(entityType: EntityType, uriPathWithId: UriPath, entityData: AnyRef, crudContext: CrudContext): Future[PortableValue] = {
    cachedFuturePortableValueOrCalculate(entityType, uriPathWithId, crudContext) {
      calculatePortableValue(entityType, uriPathWithId, entityData, crudContext)
    }
  }

  protected def calculatePortableValue(entityType: EntityType, uriPathWithId: UriPath, crudContext: CrudContext): PortableValue = {
    crudContext.withEntityPersistence(entityType) { persistence =>
      val entityOpt = persistence.find(uriPathWithId)
      entityOpt.map { entityData =>
        calculatePortableValue(entityType, uriPathWithId, entityData, crudContext)
      }
    }.getOrElse(PortableValue.empty)
  }

  protected def calculatePortableValue(entityType: EntityType, uriPathWithId: UriPath, entityData: AnyRef, crudContext: CrudContext): PortableValue = {
    val requestContext = GetterInput(uriPathWithId, crudContext, PortableField.UseDefaults)
    debug("Copying " + entityType.entityName + "#" + entityType.IdField.getRequired(entityData))
    entityType.copyFrom(entityData +: requestContext)
  }
}
