package com.github.scrud.android

import com.github.scrud.android.action._
import com.github.scrud.action._
import _root_.android.view.MenuItem
import _root_.android.content.Intent
import com.github.scrud.util.Common
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidConversions._
import _root_.android.os.Bundle
import com.github.triangle._
import view.AndroidResourceAnalyzer._
import view._
import _root_.android.app.Activity
import com.github.scrud._
import com.github.scrud.state.DestroyStateListener
import com.github.scrud.persistence.{DataListener, CrudPersistence, PersistenceFactory}
import _root_.android.widget.{AdapterView, Adapter}
import java.util.concurrent.atomic.AtomicReference
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.action.Action
import com.github.scrud.action.CrudOperation
import view.OnClickOperationSetter

/** Support for the different Crud Activity's.
  * @author Eric Pabst (epabst@gmail.com)
  */

protected trait BaseCrudActivity extends OptionsMenuActivity with Logging { self =>
  lazy val crudApplication: CrudApplication = super.getApplication.asInstanceOf[CrudAndroidApplication].application

  lazy val platformDriver: AndroidPlatformDriver = crudApplication.platformDriver.asInstanceOf[AndroidPlatformDriver]

  lazy val entityType: EntityType = crudApplication.allEntityTypes.find(entityType => Some(entityType.entityName) == currentUriPath.lastEntityNameOption).getOrElse {
    throw new IllegalStateException("No valid entityName in " + currentUriPath)
  }

  def entityName = entityType.entityName

  protected lazy val persistenceFactory: PersistenceFactory = crudApplication.persistenceFactory(entityType)

  protected[this] val createdId: AtomicReference[Option[ID]] = new AtomicReference(None)

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  private[this] lazy val initialUriPath: UriPath = {
    // The primary EntityType is used as the default starting point.
    val defaultContentUri = toUriPath(baseUriFor(crudApplication)) / crudApplication.primaryEntityType.entityName
    // If no data was given in the intent (e.g. because we were started as a MAIN activity),
    // then use our default content provider.
    Option(getIntent).flatMap(intent => Option(intent.getData).map(toUriPath(_))).getOrElse(defaultContentUri)
  }

  /** not a lazy val since dynamic in that CrudActivity.saveBasedOnUserAction sets the ID. */
  def currentUriPath: UriPath = createdId.get().map(initialUriPath / _).getOrElse(initialUriPath)

  lazy val currentCrudOperation: CrudOperation = CrudOperation(entityName, currentCrudOperationType)

  // not a val because it isn't worth storing
  protected lazy val currentCrudOperationType: CrudOperationType.Value = currentAction match {
    case AndroidOperation.CreateActionName => CrudOperationType.Create
    case AndroidOperation.ListActionName => CrudOperationType.List
    // This would normally be Operation.ListActionName, but it is the starting intent.
    case Intent.ACTION_MAIN => CrudOperationType.List
    case AndroidOperation.DisplayActionName => CrudOperationType.Read
    case AndroidOperation.UpdateActionName => CrudOperationType.Update
    case AndroidOperation.DeleteActionName => CrudOperationType.Delete
  }

  lazy val currentAction: String = getIntent.getAction

  def uriWithId(id: ID): UriPath = currentUriPath.specify(entityName, id)

  lazy val crudContext = new AndroidCrudContext(this, crudApplication)

  //final since only here as a convenience method.
  final def stateHolder = crudContext.stateHolder

  lazy val contextItems: CrudContextItems = new CrudContextItems(currentUriPath, crudContext, PortableField.UseDefaults)

  // not a val because not used enough to store
  def contextItemsWithoutUseDefaults: CrudContextItems = new CrudContextItems(currentUriPath, crudContext)

  protected lazy val logTag = Common.tryToEvaluate(crudApplication.name).getOrElse(Common.logTag)

  protected lazy val normalActions = crudApplication.actionsFromCrudOperation(currentCrudOperation)

  def populateFromUri(entityType: EntityType, uri: UriPath) {
    populateFromUri(entityType, uri, UpdaterInput(this, contextItems))
  }

  def populateFromUri(entityType: EntityType, uri: UriPath, updaterInput: UpdaterInput[AnyRef,Nothing]) {
    val futurePortableValue = crudApplication.futurePortableValue(entityType, uri, crudContext)
    if (futurePortableValue.isSet) {
      val portableValue = futurePortableValue.apply()
      debug("Copying " + portableValue + " into " + updaterInput.subject + " uri=" + uri)
      portableValue.update(updaterInput)
    } else {
      entityType.loadingValue.update(updaterInput)
      futurePortableValue.foreach { portableValue =>
        crudContext.runOnUiThread {
          debug("Copying " + portableValue + " into " + updaterInput.subject + " uri=" + uri)
          portableValue.update(updaterInput)
        }
      }
    }
  }

  lazy val entityNameLayoutPrefix = crudApplication.entityNameLayoutPrefixFor(entityName)

  private def rLayoutClasses = crudApplication.rLayoutClasses

  protected def getLayoutKey(layoutName: String): LayoutKey =
    findResourceIdWithName(rLayoutClasses, layoutName).getOrElse {
      rLayoutClasses.foreach(layoutClass => logError("Contents of " + layoutClass + " are " + layoutClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.layout." + layoutName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
              rLayoutClasses.mkString("(layout classes: ", ",", ")"))
    }

  lazy val headerLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_header")
  lazy val listLayout: LayoutKey = findResourceIdWithName(rLayoutClasses, entityNameLayoutPrefix + "_list").getOrElse(getLayoutKey("entity_list"))

  protected def listViewName: String = entityName + "_list"
  lazy val listViewKey: ViewKey = resourceIdWithName(crudApplication.rIdClasses, listViewName, "id")
  lazy val emptyListViewKeyOpt: Option[ViewKey] = findResourceIdWithName(crudApplication.rIdClasses, entityName + "_emptyList")

  lazy val rowLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_row")
  /** The layout used for each entity when allowing the user to pick one of them. */
  lazy val pickLayout: LayoutKey = pickLayoutFor(entityName)
  lazy val entryLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_entry")

  /** The layout used for each entity when allowing the user to pick one of them. */
  def pickLayoutFor(entityName: EntityName): LayoutKey = {
    findResourceIdWithName(rLayoutClasses, crudApplication.entityNameLayoutPrefixFor(entityName) + "_pick").getOrElse(
      _root_.android.R.layout.simple_spinner_dropdown_item)
  }

  // not a val because it is dynamic
  protected def applicableActions: List[Action] = LastUndoable.get(crudContext.stateHolder).map(_.undoAction).toList ++ normalActions

  protected lazy val normalOperationSetters: FieldList = {
    val setters = normalActions.map(action =>
      ViewField.viewId[Nothing](ViewRef(action.command.commandId.idString + "_command", crudApplication.rIdClasses),
        OnClickOperationSetter(_ => action.operation)))
    FieldList.toFieldList(setters)
  }

  protected def bindNormalActionsToViews() {
    normalOperationSetters.defaultValue.update(this, contextItems)
  }

  private[android] def onCommandsChanged() {
    optionsMenuCommands = generateOptionsMenu.map(_.command)
  }

  // not a val because it is dynamic
  protected def generateOptionsMenu: List[Action] =
    applicableActions.filter(action => action.command.title.isDefined || action.command.icon.isDefined)

  // not a val because it is dynamic
  def defaultOptionsMenuCommands = generateOptionsMenu.map(_.command)

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    crudContext.withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = generateOptionsMenu
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) =>
          action.invoke(currentUriPath, crudContext)
          if (LastUndoable.get(crudContext.stateHolder).exists(_.undoAction.commandId == item.getItemId)) {
            LastUndoable.clear(crudContext.stateHolder)
            onCommandsChanged()
          }
          true
        case None => super.onOptionsItemSelected(item)
      }
    }
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    crudContext.withExceptionReporting {
      // This is after the super call so that outState can be overridden if needed.
      crudContext.onSaveActivityState(outState)
    }
  }

  override def onRestoreInstanceState(savedInstanceState: Bundle) {
    crudContext.withExceptionReporting {
      // This is before the super call to be the opposite order as onSaveInstanceState.
      crudContext.onRestoreActivityState(savedInstanceState)
    }
    super.onRestoreInstanceState(savedInstanceState)
  }

  override def onResume() {
    crudContext.withExceptionReporting {
      trace("onResume")
      crudContext.onClearActivityState(stayActive = true)
    }
    super.onResume()
  }

  override def onDestroy() {
    crudContext.withExceptionReporting {
      crudContext.activityState.onDestroyState()
    }
    super.onDestroy()
  }

  def addDataListener(listener: DataListener, crudContext: CrudContext) {
    persistenceFactory.addListener(listener, entityType, crudContext)
  }

  final def setListAdapterUsingUri(crudContext: AndroidCrudContext, activity: CrudActivity) {
    setListAdapter(activity.getAdapterView, entityType, crudContext, activity.contextItems, activity, rowLayout)
  }

  private def createAdapter(persistence: CrudPersistence, contextItems: CrudContextItems, activity: Activity, itemLayout: LayoutKey): AdapterCaching = {
    val itemViewInflater = new ViewInflater(itemLayout, activity.getLayoutInflater)
    new EntityAdapterFactory().createAdapter(persistence, contextItems, itemViewInflater)
  }

  private def setListAdapter[A <: Adapter](adapterView: AdapterView[A], persistence: CrudPersistence, crudContext: AndroidCrudContext, contextItems: CrudContextItems, activity: Activity, itemLayout: LayoutKey) {
    addDataListener(new DataListener {
      def onChanged(uri: UriPath) {
        contextItems.application.FuturePortableValueCache.get(contextItems.stateHolder).clear()
      }
    }, contextItems.crudContext)
    adapterView.setAdapter(createAdapter(persistence, contextItems, activity, itemLayout).asInstanceOf[A])
    crudContext.addCachedActivityStateListener(new AdapterCachingStateListener(contextItems.crudContext))
  }

  def setListAdapter[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, crudContext: AndroidCrudContext, contextItems: CrudContextItems, activity: Activity, itemLayout: LayoutKey) {
    val persistence = crudContext.openEntityPersistence(entityType)
    crudContext.activityState.addListener(new DestroyStateListener {
      def onDestroyState() {
        persistence.close()
      }
    })
    setListAdapter(adapterView, persistence, crudContext, contextItems, activity, itemLayout)
  }

  def waitForWorkInProgress() {
    crudContext.waitForWorkInProgress()
  }

  override val toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}
