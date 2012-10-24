package com.github.scrud

import _root_.android.R
import _root_.android.app.Activity
import com.github.scrud.android._
import com.github.scrud.android.action._
import com.github.scrud.android.action.Operation._
import persistence.{PersistenceFactory, CrudPersistence}
import platform.PlatformTypes._
import state.LazyApplicationVal
import util.{Common, UrgentFutureExecutor}
import java.util.NoSuchElementException
import com.github.triangle.{PortableField, GetterInput, PortableValue, Logging}
import scala.actors.Future
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import view.AndroidResourceAnalyzer._
import view.ViewRef
import PortableField.toSome

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

trait CrudApplication extends Logging {
  def logTag = Common.tryToEvaluate(nameId).getOrElse(Common.logTag)

  trace("Instantiated CrudApplication: " + this)

  def name: String

  /** The version of the data such as a database.  This must be increased when new tables or columns need to be added, etc. */
  def dataVersion: Int

  //this will be used for programmatic uses such as a database name
  lazy val nameId: String = name.replace(" ", "_").toLowerCase

  def classNamePrefix: String = getClass.getSimpleName.replace("$", "").stripSuffix("Application")
  def packageName: String = getClass.getPackage.getName

  /** All entities in the application, in order as shown to users. */
  protected def allCrudTypes: Seq[CrudType]
  def allEntityTypes: Seq[EntityType] = allCrudTypes.map(_.entityType)

  /** The EntityType for the first page of the App. */
  def primaryEntityType: EntityType = allEntityTypes.head

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

  final def withEntityPersistence[T](entityType: EntityType, activity: ActivityWithState)(f: CrudPersistence => T): T = {
    new CrudContext(activity, this).withEntityPersistence(entityType)(f)
  }

  private def crudType(entityName: EntityName): CrudType =
    allCrudTypes.find(_.entityType.entityName == entityName).getOrElse(throw new NoSuchElementException(entityName + " not found"))

  def persistenceFactory(entityName: EntityName): PersistenceFactory = crudType(entityName).persistenceFactory
  def persistenceFactory(entityType: EntityType): PersistenceFactory = persistenceFactory(entityType.entityName)

  def newWritable(entityType: EntityType): AnyRef = persistenceFactory(entityType).newWritable

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance. */
  def maySpecifyEntityInstance(uri: UriPath, entityType: EntityType): Boolean =
    persistenceFactory(entityType).maySpecifyEntityInstance(entityType, uri)

  def isListable(entityType: EntityType): Boolean = persistenceFactory(entityType).canList
  def isListable(entityName: EntityName): Boolean = persistenceFactory(entityName).canList

  def isSavable(entityType: EntityType): Boolean = persistenceFactory(entityType).canSave
  def isSavable(entityName: EntityName): Boolean = persistenceFactory(entityName).canSave

  def isCreatable(entityName: EntityName): Boolean = persistenceFactory(entityName).canCreate
  def isCreatable(entityType: EntityType): Boolean = persistenceFactory(entityType).canCreate

  def isDeletable(entityType: EntityType): Boolean = persistenceFactory(entityType).canDelete
  def isDeletable(entityName: EntityName): Boolean = persistenceFactory(entityName).canDelete

  def activityClass = classOf[CrudActivity]
  def listActivityClass = classOf[CrudListActivity]

  private lazy val classInApplicationPackage: Class[_] = allEntityTypes.head.getClass
  def rStringClasses: Seq[Class[_]] = detectRStringClasses(classInApplicationPackage)
  private lazy val rStringClassesVal = rStringClasses
  def rIdClasses: Seq[Class[_]] = detectRIdClasses(classInApplicationPackage)
  private lazy val rIdClassesVal = rIdClasses
  def rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)
  lazy val rLayoutClassesVal = rLayoutClasses

  protected def getStringKey(stringName: String): SKey =
    findResourceIdWithName(rStringClassesVal, stringName).getOrElse {
      rStringClassesVal.foreach(rStringClass => logError("Contents of " + rStringClass + " are " + rStringClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.string." + stringName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
              rStringClassesVal.mkString("(string classes: ", ",", ")"))
    }

  def entityNameLayoutPrefixFor(entityName: EntityName) = NamingConventions.toLayoutPrefix(entityName)

  def commandToListItems(entityName: EntityName): Command = Command(None,
    findResourceIdWithName(rStringClassesVal, entityNameLayoutPrefixFor(entityName) + "_list"), None)

  def commandToDisplayItem(entityName: EntityName): Command = Command(None, None, None)

  def commandToAddItem(entityName: EntityName): Command = Command(R.drawable.ic_menu_add,
    getStringKey("add_" + entityNameLayoutPrefixFor(entityName)),
    Some(ViewRef("add_" + entityNameLayoutPrefixFor(entityName) + "_command", rIdClassesVal)))

  def commandToEditItem(entityName: EntityName): Command = Command(R.drawable.ic_menu_edit,
    getStringKey("edit_" + entityNameLayoutPrefixFor(entityName)), None)

  def commandToDeleteItem(entityName: EntityName): Command = Command(R.drawable.ic_menu_delete, res.R.string.delete_item, None)

  def displayLayoutFor(entityName: EntityName): Option[LayoutKey] = findResourceIdWithName(rLayoutClassesVal, entityNameLayoutPrefixFor(entityName) + "_display")
  def hasDisplayPage(entityName: EntityName) = displayLayoutFor(entityName).isDefined

  protected def entityOperation(entityName: EntityName, action: String, activityClass: Class[_ <: Activity]) =
    new StartEntityIdActivityOperation(entityName, action, activityClass)

  /**
   * Gets the actions that a user can perform from a specific entity instance.
   * The first one is the one that will be used when the item is clicked on.
   * May be overridden to modify the list of actions.
   */
  def actionsForEntity(entityType: EntityType): Seq[Action] =
    getReadOnlyEntityActions(entityType) ++ actionToUpdate(entityType).toSeq ++
      actionToDelete(entityType).toSeq

  protected def getReadOnlyEntityActions(entityType: EntityType): Seq[Action] =
    actionToDisplay(entityType).toSeq ++ childEntityTypes(entityType).flatMap(actionToList(_))

  /**
   * Gets the actions that a user can perform from a list of the entities.
   * May be overridden to modify the list of actions.
   */
  def actionsForList(entityType: EntityType): Seq[Action] = actionsForList(entityType.entityName)

  /**
   * Gets the actions that a user can perform from a list of the entities.
   * May be overridden to modify the list of actions.
   */
  def actionsForList(entityName: EntityName): Seq[Action] =
    readOnlyActionsForList(entityName) ++ actionToCreate(entityName).toSeq

  protected def readOnlyActionsForList(entityName: EntityName): Seq[Action] = {
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
      Some(Action(commandToAddItem(entityName), new StartEntityActivityOperation(entityName, CreateActionName, activityClass)))
    else
      None
  }

  /** Gets the action to display a UI for a user to edit data for an entity given its id in the UriPath. */
  def actionToUpdate(entityType: EntityType): Option[Action] = actionToUpdate(entityType.entityName)
  def actionToUpdate(entityName: EntityName): Option[Action] = {
    if (isSavable(entityName)) Some(Action(commandToEditItem(entityName), entityOperation(entityName, UpdateActionName, activityClass)))
    else None
  }

  def actionToDelete(entityType: EntityType): Option[Action] = {
    if (isDeletable(entityType)) {
      Some(Action(commandToDeleteItem(entityType.entityName), StartEntityDeleteOperation(entityType)))
    } else None
  }

  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityType: EntityType): Option[Action] = actionToList(entityType.entityName)
  /** Gets the action to display the list that matches the criteria copied from criteriaSource using entityType.copy. */
  def actionToList(entityName: EntityName): Option[Action] =
    Some(Action(commandToListItems(entityName), new StartEntityActivityOperation(entityName, ListActionName, listActivityClass)))

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityType: EntityType): Option[Action] = actionToDisplay(entityType.entityName)

  /** Gets the action to display the entity given the id in the UriPath. */
  def actionToDisplay(entityName: EntityName): Option[Action] = {
    if (hasDisplayPage(entityName)) {
      Some(Action(commandToDisplayItem(entityName), entityOperation(entityName, DisplayActionName, activityClass)))
    } else None
  }

  private[scrud] object FuturePortableValueCache
    extends LazyApplicationVal[mutable.ConcurrentMap[(EntityType, UriPath, CrudContext),Future[PortableValue]]](new ConcurrentHashMap[(EntityType, UriPath, CrudContext),Future[PortableValue]]())

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
