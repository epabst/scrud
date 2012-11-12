package com.github.scrud.android

import action._
import action.Command
import com.github.scrud.action.{PersistenceOperation, Action, CrudOperationType, CrudOperation}
import android.view.{View, MenuItem}
import android.content.{Context, Intent}
import com.github.scrud.util.Common
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidConversions._
import android.os.Bundle
import com.github.triangle._
import persistence.{EntityTypePersistedInfo, CursorStream}
import view.AndroidResourceAnalyzer._
import view._
import android.app.Activity
import com.github.scrud.{EntityType, UriPath, CrudApplication}
import com.github.scrud.state.{DestroyStateListener, StateVar}
import com.github.scrud.persistence.{DataListener, CrudPersistence, PersistenceFactory}
import android.widget.{ResourceCursorAdapter, AdapterView, Adapter}
import android.database.Cursor
import com.github.scrud.EntityName
import scala.Some
import view.OnClickOperationSetter

/** Support for the different Crud Activity's.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait BaseCrudActivity extends ActivityWithState with OptionsMenuActivity with Logging { self =>
  lazy val platformDriver = AndroidPlatformDriver

  lazy val crudApplication: CrudApplication = super.getApplication.asInstanceOf[CrudAndroidApplication].application

  lazy val entityType = crudApplication.allEntityTypes.find(entityType => Some(entityType.entityName.name) == currentUriPath.lastEntityNameOption).getOrElse {
    throw new IllegalStateException("No valid entityName in " + currentUriPath)
  }

  def entityName = entityType.entityName

  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The fields must support copying into this object.
    */
  def newWritable() = persistenceFactory.newWritable()

  protected lazy val persistenceFactory: PersistenceFactory = crudApplication.persistenceFactory(entityType)

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  lazy val currentUriPath: UriPath = {
    val defaultContentUri = crudApplication.defaultContentUri
    Option(getIntent).map(intent => Option(intent.getData).map(toUriPath(_)).getOrElse {
      // If no data was given in the intent (because we were started
      // as a MAIN activity), then use our default content provider.
      intent.setData(defaultContentUri)
      defaultContentUri
    }).getOrElse(defaultContentUri)
  }

  lazy val currentCrudOperation: CrudOperation = CrudOperation(entityName, currentCrudOperationType)

  // not a val because it isn't worth storing
  private def currentCrudOperationType: CrudOperationType.Value = currentAction match {
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

  lazy val contextItems = GetterInput(currentUriPath, crudContext, PortableField.UseDefaults)

  // not a val because not used enough to store
  def contextItemsWithoutUseDefaults = GetterInput(currentUriPath, crudContext)

  protected lazy val logTag = Common.tryToEvaluate(crudApplication.name).getOrElse(Common.logTag)

  /** This should be a lazy val in subclasses. */
  protected def normalActions: Seq[Action]

  def populateFromUri(entityType: EntityType, uri: UriPath) {
    populateFromUri(entityType, uri, UpdaterInput(this, contextItems))
  }

  def populateFromUri(entityType: EntityType, uri: UriPath, updaterInput: UpdaterInput[AnyRef,Nothing]) {
    val futurePortableValue = crudApplication.futurePortableValue(entityType, uri, crudContext)
    if (futurePortableValue.isSet) {
      futurePortableValue().update(updaterInput)
    } else {
      entityType.loadingValue.update(updaterInput)
      futurePortableValue.foreach { portableValue =>
        crudContext.runOnUiThread {
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
  lazy val listLayout: LayoutKey =
    findResourceIdWithName(rLayoutClasses, entityNameLayoutPrefix + "_list").getOrElse(getLayoutKey("entity_list"))
  lazy val rowLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_row")
  /** The layout used for each entity when allowing the user to pick one of them. */
  lazy val pickLayout: LayoutKey = pickLayoutFor(entityName)
  lazy val entryLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_entry")

  /** The layout used for each entity when allowing the user to pick one of them. */
  def pickLayoutFor(entityName: EntityName): LayoutKey = {
    findResourceIdWithName(rLayoutClasses, crudApplication.entityNameLayoutPrefixFor(entityName) + "_pick").getOrElse(
      _root_.android.R.layout.simple_spinner_dropdown_item)
  }

  lazy val commandToUndoDelete = Command(None, Some(res.R.string.undo_delete), None)

  /** Delete an entity by Uri with an undo option.  It can be overridden to do a confirmation box if desired. */
  def startDelete(entityType: EntityType, uri: UriPath, activity: BaseCrudActivity) {
    crudContext.withEntityPersistence(entityType)(undoableDelete(uri))
  }

  private[scrud] def undoableDelete(uri: UriPath)(persistence: CrudPersistence) {
    persistence.find(uri).foreach { readable =>
      val id = entityType.IdField.getValue(readable)
      val writable = entityType.copyAndUpdate(readable, newWritable)
      persistence.delete(uri)
      val undoDeleteOperation = new PersistenceOperation(entityType, persistence.crudContext.application) {
        def invoke(uri: UriPath, persistence: CrudPersistence) {
          persistence.save(id, writable)
        }
      }
      //todo delete childEntities recursively
      val context = crudContext.activityContext
      context match {
        case activity: BaseCrudActivity =>
          activity.allowUndo(Undoable(Action(commandToUndoDelete, undoDeleteOperation), None))
        case _ =>
      }
    }
  }

  /** A StateVar that holds an undoable Action if present. */
  private object LastUndoable extends StateVar[Undoable]

  def allowUndo(undoable: Undoable) {
    // Finish any prior undoable first.  This could be re-implemented to support a stack of undoable operations.
    LastUndoable.clear(this).foreach(_.closeOperation.foreach(_.invoke(currentUriPath, this)))
    // Remember the new undoable operation
    LastUndoable.set(this, undoable)
    optionsMenuCommands = generateOptionsMenu.map(_.command)
  }

  // not a val because it is dynamic
  protected def applicableActions: List[Action] = LastUndoable.get(this).map(_.undoAction).toList ++ normalActions

  protected lazy val normalOperationSetters: FieldList = {
    val setters = normalActions.filter(_.command.viewRef.isDefined).map(action =>
      ViewField.viewId[Nothing](action.command.viewRef.get, OnClickOperationSetter(_ => action.operation)))
    FieldList.toFieldList(setters)
  }

  protected def bindNormalActionsToViews() {
    normalOperationSetters.defaultValue.update(this, contextItems)
  }

  // not a val because it is dynamic
  protected def generateOptionsMenu: List[Action] =
    applicableActions.filter(action => action.command.title.isDefined || action.command.icon.isDefined)

  // not a val because it is dynamic
  def initialOptionsMenuCommands = generateOptionsMenu.map(_.command)

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    crudContext.withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = generateOptionsMenu
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) =>
          action.invoke(currentUriPath, crudContext)
          if (LastUndoable.get(this).exists(_.undoAction.commandId == item.getItemId)) {
            LastUndoable.clear(this)
            optionsMenuCommands = generateOptionsMenu.map(_.command)
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

  def addDataListener(listener: DataListener, crudContext: AndroidCrudContext) {
    persistenceFactory.addListener(listener, entityType, crudContext)
  }

  final def setListAdapterUsingUri(crudContext: AndroidCrudContext, activity: CrudListActivity) {
    setListAdapter(activity.getListView, entityType, activity.currentUriPath, crudContext, activity.contextItems, activity, rowLayout)
  }

  private def createAdapter[A <: Adapter](persistence: CrudPersistence, uriPath: UriPath, entityType: EntityType, crudContext: AndroidCrudContext, contextItems: GetterInput, activity: Activity, itemLayout: LayoutKey, adapterView: AdapterView[A]): AdapterCaching = {
    val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
    val findAllResult = persistence.findAll(uriPath)
    findAllResult match {
      case CursorStream(cursor, _) =>
        activity.startManagingCursor(cursor)
        addDataListener(new DataListener {
          def onChanged(uri: UriPath) {
            cursor.requery()
          }
        }, crudContext)
        new ResourceCursorAdapter(activity, itemLayout, cursor) with AdapterCaching {
          def crudContext = self.crudContext

          def entityType = self.entityType

          /** The UriPath that does not contain the entities. */
          protected def uriPathWithoutEntityId = uriPath

          def bindView(view: View, context: Context, cursor: Cursor) {
            val row = entityTypePersistedInfo.copyRowToMap(cursor)
            bindViewFromCacheOrItems(view, cursor.getPosition, row, adapterView, self.crudContext, contextItems)
          }
        }
      case _ => new EntityAdapter(entityType, findAllResult, itemLayout, contextItems, activity.getLayoutInflater)
    }
  }

  private def setListAdapter[A <: Adapter](adapterView: AdapterView[A], persistence: CrudPersistence, uriPath: UriPath, entityType: EntityType, crudContext: AndroidCrudContext, contextItems: GetterInput, activity: Activity, itemLayout: LayoutKey) {
    addDataListener(new DataListener {
      def onChanged(uri: UriPath) {
        crudContext.application.FuturePortableValueCache.get(crudContext).clear()
      }
    }, crudContext)
    def callCreateAdapter(): A = {
      createAdapter(persistence, uriPath, entityType, crudContext, contextItems, activity, itemLayout, adapterView).asInstanceOf[A]
    }
    val adapter = callCreateAdapter()
    adapterView.setAdapter(adapter)
    crudContext.addCachedActivityStateListener(new AdapterCachingStateListener(adapterView, entityType, crudContext, adapterFactory = callCreateAdapter()))
  }

  def setListAdapter[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, uriPath: UriPath, crudContext: AndroidCrudContext, contextItems: GetterInput, activity: Activity, itemLayout: LayoutKey) {
    val persistence = crudContext.openEntityPersistence(entityType)
    crudContext.activityState.addListener(new DestroyStateListener {
      def onDestroyState() {
        persistence.close()
      }
    })
    setListAdapter(adapterView, persistence, uriPath, entityType, crudContext, contextItems, activity, itemLayout)
  }

  def waitForWorkInProgress() {
    crudContext.waitForWorkInProgress()
  }

  override val toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}

/** An undo of an operation.  The operation should have already completed, but it can be undone or accepted.
  * @param undoAction  An Action that reverses the operation.
  * @param closeOperation  An operation that releases any resources, and is guaranteed to be called.
  *           For example, deleting related entities if undo was not called.
  */
case class Undoable(undoAction: Action, closeOperation: Option[AndroidOperation] = None)
