package com.github.scrud

import _root_.android.R
import action._
import action.Action
import action.Command
import action.CommandId
import action.CrudOperation
import action.StartEntityDeleteOperation
import persistence.PersistenceFactory
import platform.PlatformDriver
import platform.PlatformTypes._
import com.github.triangle._
import com.github.scrud
import scrud.state.LazyApplicationVal
import util.{Common, UrgentFutureExecutor}
import java.util.NoSuchElementException
import scala.actors.Future
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import PortableField.toSome
import scrud.android.{CrudType,NamingConventions,res}
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

abstract class CrudApplication(val platformDriver: PlatformDriver) extends Logging {
  lazy val logTag = Common.tryToEvaluate(nameId).getOrElse(Common.logTag)

  def name: String

  /** The version of the data such as a database.  This must be increased when new tables or columns need to be added, etc. */
  def dataVersion: Int

  //this will be used for programmatic uses such as a database name
  lazy val nameId: String = name.replace(" ", "_").toLowerCase

  val classNamePrefix: String = getClass.getSimpleName.replace("$", "").stripSuffix("Application")
  val packageName: String = getClass.getPackage.getName

  /** All entities in the application, in order as shown to users. */
  protected def allCrudTypes: Seq[CrudType]
  def allEntityTypes: Seq[EntityType] = allCrudTypes.map(_.entityType)

  /** The EntityType for the first page of the App. */
  lazy val primaryEntityType: EntityType = allEntityTypes.head

  def entityType(entityName: EntityName): EntityType = allEntityTypes.find(_.entityName == entityName).getOrElse {
    throw new IllegalArgumentException("Unknown entity: entityName=" + entityName)
  }

  lazy val contentProviderAuthority = packageName
  // The first EntityType is used as the default starting point.
  lazy val defaultContentUri = UriPath("content://" + contentProviderAuthority) / primaryEntityType.entityName

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

  def persistenceFactory(entityName: EntityName): PersistenceFactory = crudType(entityName).persistenceFactory
  def persistenceFactory(entityType: EntityType): PersistenceFactory = persistenceFactory(entityType.entityName)

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance. */
  def maySpecifyEntityInstance(uri: UriPath, entityType: EntityType): Boolean =
    persistenceFactory(entityType).maySpecifyEntityInstance(entityType.entityName, uri)

  def isListable(entityType: EntityType): Boolean = persistenceFactory(entityType).canList
  def isListable(entityName: EntityName): Boolean = persistenceFactory(entityName).canList

  def isSavable(entityType: EntityType): Boolean = persistenceFactory(entityType).canSave
  def isSavable(entityName: EntityName): Boolean = persistenceFactory(entityName).canSave

  def isCreatable(entityName: EntityName): Boolean = persistenceFactory(entityName).canCreate
  def isCreatable(entityType: EntityType): Boolean = persistenceFactory(entityType).canCreate

  def isDeletable(entityType: EntityType): Boolean = persistenceFactory(entityType).canDelete
  def isDeletable(entityName: EntityName): Boolean = persistenceFactory(entityName).canDelete

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
    R.drawable.ic_menu_add,
    getStringKey("add_" + entityNameLayoutPrefixFor(entityName)))

  def commandToEditItem(entityName: EntityName): Command = Command(CommandId("edit_" + entityNameLayoutPrefixFor(entityName)),
    R.drawable.ic_menu_edit, getStringKey("edit_" + entityNameLayoutPrefixFor(entityName)))

  def commandToDeleteItem(entityName: EntityName): Command = Command(CommandId("delete_" + entityNameLayoutPrefixFor(entityName)),
    R.drawable.ic_menu_delete, res.R.string.delete_item)

  def displayLayoutFor(entityName: EntityName): Option[LayoutKey] = findResourceIdWithName(rLayoutClasses, entityNameLayoutPrefixFor(entityName) + "_display")
  def hasDisplayPage(entityName: EntityName) = displayLayoutFor(entityName).isDefined

  /**
   * Gets the actions that a user can perform from a given CrudOperation.
   * May be overridden to modify the list of actions.
   */
  def actionsFromCrudOperation(crudOperation: CrudOperation): Seq[Action] = (crudOperation match {
    case CrudOperation(entityName, CrudOperationType.Create) =>
      actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.Read) =>
      childEntityNames(entityName).flatMap(actionToList(_)) ++
          actionToUpdate(entityName).toSeq ++ actionToDelete(entityName).toSeq
    case CrudOperation(entityName, CrudOperationType.List) =>
      actionToCreate(entityName).toSeq ++ actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName)
    case CrudOperation(entityName, CrudOperationType.Update) =>
      actionToDisplay(entityName).toSeq ++ actionToDelete(entityName).toSeq
  })

  protected def actionsToUpdateAndListChildrenOfOnlyParentWithoutDisplayAction(entityName: EntityName): Seq[Action] = {
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


  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionToCreate(entityType: EntityType): Option[Action] = actionToCreate(entityType.entityName)

  /** Gets the action to display a UI for a user to fill in data for creating an entity.
    * The target Activity should copy Unit into the UI using entityType.copy to populate defaults.
    */
  def actionToCreate(entityName: EntityName): Option[Action] = {
    if (isCreatable(entityName))
      Some(Action(commandToAddItem(entityName), platformDriver.operationToShowCreateUI(entityName)))
    else
      None
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionToUpdate(entityType: EntityType): Option[Action] = actionToUpdate(entityType.entityName)
  def actionToUpdate(entityName: EntityName): Option[Action] = {
    if (isSavable(entityName)) Some(Action(commandToEditItem(entityName), platformDriver.operationToShowUpdateUI(entityName)))
    else None
  }

  def actionToDelete(entityName: EntityName): Option[Action] = actionToDelete(entityType(entityName))
  def actionToDelete(entityType: EntityType): Option[Action] = {
    if (isDeletable(entityType)) {
      Some(Action(commandToDeleteItem(entityType.entityName), StartEntityDeleteOperation(entityType)))
    } else None
  }

  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityType: EntityType): Option[Action] = actionToList(entityType.entityName)
  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityName: EntityName): Option[Action] =
    Some(Action(commandToListItems(entityName), platformDriver.operationToShowListUI(entityName)))

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityType: EntityType): Option[Action] = actionToDisplay(entityType.entityName)

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityName: EntityName): Option[Action] = {
    if (hasDisplayPage(entityName)) {
      Some(Action(commandToDisplayItem(entityName), platformDriver.operationToShowDisplayUI(entityName)))
    } else None
  }

  private[scrud] object FuturePortableValueCache
    extends LazyApplicationVal[mutable.ConcurrentMap[(EntityType, UriPath, CrudContext),Future[PortableValue]]](new ConcurrentHashMap[(EntityType, UriPath, CrudContext),Future[PortableValue]]())

  /**
   * Save the data into the persistence for entityType.
   * If data is invalid (based on updating a ValidationResult), returns None, otherwise returns the created or updated ID.
   */
  def saveIfValid(data: AnyRef, entityType: EntityType, contextItems: CrudContextItems): Option[ID] = {
    val crudContext = contextItems.crudContext
    val updaterInput = UpdaterInput(crudContext.newWritable(entityType), contextItems)
    val relevantFields = entityType.copyableTo(updaterInput)
    val portableValue = relevantFields.copyFrom(data +: contextItems)
    if (portableValue.update(ValidationResult.Valid).isValid) {
      val updatedWritable = portableValue.update(updaterInput)
      val idOpt = entityType.IdField(contextItems.currentUriPath)
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
    val cache = FuturePortableValueCache.get(crudContext)
    val key = (entityType, uriPathWithId, crudContext)
    cache.get(key).getOrElse {
      val futurePortableValue = executor.urgentFuture {
        calculate
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
    crudContext.withEntityPersistence(entityType)(_.find(uriPathWithId).map { entityData =>
      calculatePortableValue(entityType, uriPathWithId, entityData, crudContext)
    }).getOrElse(PortableValue.empty)
  }

  protected def calculatePortableValue(entityType: EntityType, uriPathWithId: UriPath, entityData: AnyRef, crudContext: CrudContext): PortableValue = {
    val contextItems = GetterInput(uriPathWithId, crudContext, PortableField.UseDefaults)
    debug("Copying " + entityType.entityName + "#" + entityType.IdField.getRequired(entityData))
    entityType.copyFrom(entityData +: contextItems)
  }
}
