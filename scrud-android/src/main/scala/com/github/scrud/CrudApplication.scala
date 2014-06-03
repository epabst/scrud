package com.github.scrud

import _root_.android.R
import action._
import action.ActionKey
import persistence.EntityTypeMap
import platform.PlatformDriver
import platform.PlatformTypes._
import state.ApplicationConcurrentMapVal
import com.github.scrud.util.{Logging, Common, UrgentFutureExecutor}
import com.github.scrud
import com.github.scrud.android.{AndroidPlatformDriver, NamingConventions}
import scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.context.CommandContext
import com.github.scrud.copy.types.ValidationResult
import com.github.scrud.copy._
import scala.concurrent.Future
import scala.Some
import com.github.scrud.action.PlatformCommand
import com.github.scrud.action.CrudOperation
import com.github.scrud.action.StartEntityDeleteOperation
import com.github.scrud.action.OperationAction
import scala.util.Try

/**
 * A stateless Application that uses Scrud.  It has all the configuration for how the application behaves,
 * but none of its actual state.
 * It that works with pairings of an [[com.github.scrud.EntityType]] and
 * a [[com.github.scrud.persistence.PersistenceFactory]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */
@deprecated("use EntityNavigation or EntityTypeMap", since = "2014-05-12")
abstract class CrudApplication(val platformDriver: PlatformDriver, val entityTypeMap: EntityTypeMap) extends Logging {
  lazy val logTag = Try(nameId).getOrElse(Common.logTag)

  final def name: String = entityTypeMap.applicationName.name

  lazy val entityNavigation: EntityNavigation = new EntityNavigation(entityTypeMap)

  //this will be used for programmatic uses such as a database name
  lazy val nameId: String = name.replace(" ", "_").toLowerCase

  val packageName: String = getClass.getPackage.getName

  private lazy val deleteItemStringKey = getStringKey("delete_item")
  private lazy val dataSavedNotificationStringKey = getStringKey("data_saved_notification")
  private lazy val dataNotSavedSinceInvalidNotificationStringKey = getStringKey("data_not_saved_since_invalid_notification")

  /** The EntityType for the first page of the App. */
  @deprecated("use entityNavigation.primaryEntityType", since = "2014-05-28")
  lazy val primaryEntityType: EntityType = entityNavigation.primaryEntityType

  @deprecated("use entityTypeMap.upstreamEntityNames(entityName)", since = "2014-05-28")
  def parentEntityNames(entityName: EntityName): Seq[EntityName] = entityTypeMap.upstreamEntityNames(entityName)

  @deprecated("use entityTypeMap.downstreamEntityNames(entityType)", since = "2014-05-28")
  def childEntityNames(entityName: EntityName): Seq[EntityName] =
    entityTypeMap.downstreamEntityNames(entityName)

  @deprecated("use entityTypeMap.downstreamEntityTypes(entityType)", since = "2014-05-28")
  def childEntityTypes(entityType: EntityType): Seq[EntityType] =
    entityTypeMap.downstreamEntityTypes(entityType)

  private lazy val classInApplicationPackage: Class[_] = entityTypeMap.allEntityTypes.head.getClass
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(classInApplicationPackage)
  lazy val rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)

  protected def getStringKey(stringName: String): SKey =
    platformDriver.asInstanceOf[AndroidPlatformDriver].getStringKey(stringName)

  def entityNameLayoutPrefixFor(entityName: EntityName) = NamingConventions.toLayoutPrefix(entityName)

  def commandToListItems(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey(entityNameLayoutPrefixFor(entityName) + "_list"), None,
    platformDriver.asInstanceOf[AndroidPlatformDriver].findStringKey(entityNameLayoutPrefixFor(entityName) + "_list"))

  def commandToDisplayItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("display_" + entityNameLayoutPrefixFor(entityName)),
    None, None)

  def commandToAddItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("add_" + entityNameLayoutPrefixFor(entityName)),
    Some(R.drawable.ic_menu_add),
    Some(getStringKey("add_" + entityNameLayoutPrefixFor(entityName))))

  def commandToEditItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("edit_" + entityNameLayoutPrefixFor(entityName)),
    Some(R.drawable.ic_menu_edit), Some(getStringKey("edit_" + entityNameLayoutPrefixFor(entityName))))

  def commandToDeleteItem(entityName: EntityName): PlatformCommand = {
    PlatformCommand(ActionKey("delete_" + entityNameLayoutPrefixFor(entityName)),
      Some(R.drawable.ic_menu_delete), Some(deleteItemStringKey))
  }

  def displayLayoutFor(entityName: EntityName): Option[LayoutKey] = findResourceIdWithName(rLayoutClasses, entityNameLayoutPrefixFor(entityName) + "_display")
  def hasDisplayPage(entityName: EntityName) = displayLayoutFor(entityName).isDefined

  /**
   * Gets the actions that a user can perform from a given CrudOperation.
   * May be overridden to modify the list of actions.
   */
  def actionsFromCrudOperation(crudOperation: CrudOperation): Seq[OperationAction] = crudOperation match {
    case CrudOperation(entityName, CrudOperationType.Create) =>
      parentEntityNames(entityName).flatMap(actionsToManage(_)) ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.Read) =>
      childEntityNames(entityName).flatMap(actionToList(_)) ++
        actionToUpdate(entityName).toSeq ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.List) =>
      actionToCreate(entityName).toSeq ++ actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName)
    case CrudOperation(entityName, CrudOperationType.Update) =>
      actionToDisplay(entityName).toSeq ++ parentEntityNames(entityName).flatMap(actionsToManage(_)) ++ actionToDelete(entityName).toSeq
  }

  protected def actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName: EntityName): Seq[OperationAction] = {
    val thisEntity = entityTypeMap.entityType(entityName)
    thisEntity.entityReferenceFieldDeclarations match {
      //exactly one parent w/o a display page
      case entityReferenceField :: Nil if !actionToDisplay(entityReferenceField.entityName).isDefined => {
        val referencedEntityType = entityTypeMap.entityType(entityReferenceField.entityName)
        //the parent's actionToUpdate should be shown since clicking on the parent entity brought the user
        //to the list of child entities instead of to a display page for the parent entity.
        actionToUpdate(referencedEntityType).toSeq ++
          childEntityTypes(referencedEntityType).filter(_ != thisEntity).flatMap(actionToList(_))
      }
      case _ => Nil
    }
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
    if (entityTypeMap.isCreatable(entityName))
      Some(OperationAction(commandToAddItem(entityName), platformDriver.operationToShowCreateUI(entityName)))
    else
      None
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionToUpdate(entityType: EntityType): Option[OperationAction] = actionToUpdate(entityType.entityName)
  def actionToUpdate(entityName: EntityName): Option[OperationAction] = {
    if (entityTypeMap.isSavable(entityName)) Some(OperationAction(commandToEditItem(entityName), platformDriver.operationToShowUpdateUI(entityName)))
    else None
  }

  def actionToDelete(entityName: EntityName): Option[OperationAction] = actionToDelete(entityTypeMap.entityType(entityName))
  def actionToDelete(entityType: EntityType): Option[OperationAction] = {
    if (entityTypeMap.isDeletable(entityType)) {
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
    extends ApplicationConcurrentMapVal[(EntityType, UriPath, CommandContext),Future[Option[AdaptedValueSeq]]]

  /**
   * Save the data into the persistence for entityType.
   * If data is invalid (based on updating a ValidationResult), returns None, otherwise returns the created or updated ID.
   */
  def saveIfValid(sourceUri: UriPath, sourceType: SourceType, source: AnyRef, entityType: EntityType, commandContext: CommandContext): Option[ID] = {
    val idOpt = entityType.idField.sourceFieldOrFail(sourceType).findValue(source, new CopyContext(sourceUri, commandContext))
    if (entityType.copyAndUpdate(sourceType, source, sourceUri, ValidationResult, commandContext).isValid) {
      val newId = commandContext.save(entityType.entityName, sourceType, idOpt, source)
      commandContext.displayMessageToUserBriefly(dataSavedNotificationStringKey)
      Some(newId)
    } else {
      commandContext.displayMessageToUserBriefly(dataNotSavedSinceInvalidNotificationStringKey)
      None
    }
  }

  private lazy val executor = new UrgentFutureExecutor()

  private def cachedFuturePortableValueOptOrCalculate(entityType: EntityType, uriPathWithId: UriPath, commandContext: CommandContext)(calculate: => Option[AdaptedValueSeq]): Future[Option[AdaptedValueSeq]] = {
    val cache = FuturePortableValueCache.get(commandContext.sharedContext)
    val key = (entityType, uriPathWithId, commandContext)
    cache.get(key).getOrElse {
      val futurePortableValueOpt = executor.urgentFuture {
        commandContext.withExceptionReportingHavingDefaultReturnValue[Option[AdaptedValueSeq]](None) {
          calculate
        }
      }
      cache.putIfAbsent(key, futurePortableValueOpt).getOrElse(futurePortableValueOpt)
    }
  }

  def futurePortableValueOpt(entityType: EntityType, sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): Future[Option[AdaptedValueSeq]] = {
    cachedFuturePortableValueOptOrCalculate(entityType, sourceUriWithId, commandContext) {
      calculatePortableValueOpt(entityType, sourceUriWithId, targetType, commandContext)
    }
  }

  def futurePortableValueOpt(entityType: EntityType, sourceType: SourceType, source: AnyRef,
                             sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): Future[Option[AdaptedValueSeq]] = {
    cachedFuturePortableValueOptOrCalculate(entityType, sourceUriWithId, commandContext) {
      Some(calculatePortableValue(entityType, sourceType, source, sourceUriWithId, targetType, commandContext))
    }
  }

  protected def calculatePortableValueOpt(entityType: EntityType, sourceUriWithId: UriPath,
                                          targetType: TargetType, commandContext: CommandContext): Option[AdaptedValueSeq] = {
    val persistence = commandContext.persistenceFor(entityType.entityName)
    val sourceOpt = persistence.find(sourceUriWithId)
    sourceOpt.map { source =>
      calculatePortableValue(entityType, persistence.sourceType, source, sourceUriWithId, targetType, commandContext)
    }
  }

  protected def calculatePortableValue(entityType: EntityType, sourceType: SourceType, source: AnyRef,
                                       sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): AdaptedValueSeq = {
    debug("Copying entityType=" + entityType + " from sourceUri=" + sourceUriWithId + 
      " from sourceType=" + sourceType + " to targetType=" + targetType)
    entityType.copy(sourceType, source, sourceUriWithId, targetType, commandContext)
  }
}
