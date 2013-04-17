package com.github.scrud.android

import _root_.android.database.Cursor
import _root_.android.support.v4.app.{LoaderManager, FragmentActivity}
import _root_.android.support.v4.content.{CursorLoader, Loader}
import android.os.Bundle
import com.github.triangle.{FieldList, UpdaterInput, GetterInput, PortableField}
import android.content.Intent
import android.app.Activity
import _root_.android.widget.{Adapter, AdapterView, ListView}
import com.github.scrud
import scrud.action.CrudOperationType
import scrud.platform.PlatformTypes
import scrud.persistence.{PersistenceFactory, DataListener}
import scrud.android.action.{AndroidOperation, OperationResponse, OptionsMenuActivity}
import scrud.android.state.CachedStateListener
import android.view.{MenuItem, View, ContextMenu}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import scrud.platform.PlatformTypes._
import java.util.concurrent.atomic.AtomicReference
import scrud.android.view.AndroidConversions._
import scrud.util.Common
import scrud.android.view.AndroidResourceAnalyzer._
import scrud.android.view._
import scrud.{android=>_,_}
import scrud.action.{Action,CrudOperation}
import scrud.android.view.OnClickOperationSetter
import scala.collection.mutable
import com.github.scrud.android.persistence.{EntityTypePersistedInfo, ContentQuery}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.asScalaConcurrentMap

/** A generic Activity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudActivity extends FragmentActivity with OptionsMenuActivity with LoaderManager.LoaderCallbacks[Cursor] { self =>
  lazy val crudApplication: CrudApplication = super.getApplication.asInstanceOf[CrudAndroidApplication].application

  lazy val platformDriver: AndroidPlatformDriver = crudApplication.platformDriver.asInstanceOf[AndroidPlatformDriver]

  lazy val entityType: EntityType = crudApplication.allEntityTypes.find(entityType => Some(entityType.entityName) == currentUriPath.lastEntityNameOption).getOrElse {
    throw new IllegalStateException("No valid entityName in " + currentUriPath)
  }

  def entityName = entityType.entityName

  protected lazy val persistenceFactory: PersistenceFactory = crudContext.persistenceFactory(entityType)

  protected[this] val createdId: AtomicReference[Option[ID]] = new AtomicReference(None)

  private[this] val cursorLoaderDataList = mutable.Buffer[CursorLoaderData]()

  private[this] val cursorLoaderDataByLoader: mutable.ConcurrentMap[Loader[Cursor],CursorLoaderData] = new ConcurrentHashMap[Loader[Cursor],CursorLoaderData]()

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  protected lazy val initialUriPath: UriPath = {
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

  private var adapterView: AdapterView[_ <: Adapter] = null

  protected val mOnClickListener: AdapterView.OnItemClickListener = new AdapterView.OnItemClickListener {
    def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
      onListItemClick(parent.asInstanceOf[AdapterView[_ <: Adapter]], v, position, id)
    }
  }
  private val mRequestFocus: Runnable = new Runnable {
    def run() {
      adapterView.focusableViewAvailable(adapterView)
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    crudContext.withExceptionReporting {
      if (savedInstanceState == null) {
        setContentViewForOperation()
        populateDataInViews()
      }
      bindActionsToViews()
      if (crudApplication.maySpecifyEntityInstance(currentUriPath, entityType)) {
        crudContext.addCachedActivityStateListener(new CachedStateListener {
          def onClearState(stayActive: Boolean) {
            if (stayActive) {
              populateFromUri(entityType, currentUriPath)
            }
          }

          def onSaveState(outState: Bundle) {
            entityType.copy(this, outState)
          }

          def onRestoreState(savedInstanceState: Bundle) {
            val portableValue = entityType.copyFrom(savedInstanceState)
            crudContext.runOnUiThread { portableValue.update(this, contextItems) }
          }
        })
      }
    }
  }

  protected def setContentViewForOperation() {
    setContentView(applicableLayout)

    currentCrudOperationType match {
      case CrudOperationType.List =>
    		getAdapterView match {
          case listView: ListView =>
            listView.setHeaderDividersEnabled(true)
            listView.addHeaderView(getLayoutInflater.inflate(headerLayout, null))
          case _ =>
        }
      case _ =>
    }
  }

  protected def applicableLayout: PlatformTypes.LayoutKey = {
    currentCrudOperationType match {
      case CrudOperationType.Create | CrudOperationType.Update =>
        entryLayout
      case CrudOperationType.List =>
        listLayout
    }
  }

  protected def bindActionsToViews() {
    bindNormalActionsToViews()
    if (currentCrudOperationType == CrudOperationType.List) {
      registerForContextMenu(getAdapterView)
    }
  }

  protected def populateDataInViews() {
    currentCrudOperationType match {
      case CrudOperationType.List =>
        setListAdapterUsingUri(crudContext, this)
        crudContext.future {
          populateFromParentEntities()
          persistenceFactory.addListener(new DataListener {
            def onChanged() {
              //Some of the parent fields may be calculated from the children
              populateFromParentEntities()
            }
          }, entityType, crudContext)
        }
      case _ =>
        val currentPath = currentUriPath
        if (crudApplication.maySpecifyEntityInstance(currentPath, entityType)) {
          populateFromUri(entityType, currentPath)
        } else {
          entityType.copy(PortableField.UseDefaults +: contextItems, this)
        }
    }
  }

  private[scrud] def populateFromParentEntities() {
    val uriPath = currentUriPath
    //copy each parent Entity's data to the Activity if identified in the currentUriPath
    entityType.parentEntityNames.foreach { parentEntityName =>
      val parentType = crudApplication.entityType(parentEntityName)
      if (crudApplication.maySpecifyEntityInstance(uriPath, parentType)) {
        populateFromUri(parentType, uriPath)
      }
    }
  }

  /** This is taken from android's ListActivity.  Not sure what situations make it necessary. */
  private def ensureAdapterView() {
    if (adapterView == null) {
      setContentView(listLayout)
    }
  }

  /**
   * Updates the screen state (current list and other views) when the
   * content changes.
   *
   * @see Activity#onContentChanged()
   */
  override def onContentChanged() {
    super.onContentChanged()
    if (currentCrudOperationType == CrudOperationType.List) {
      val emptyViewOpt: Option[View] = emptyListViewKeyOpt.flatMap(key => Option(findViewById(key)))
      this.adapterView = Option(findViewById(listViewKey).asInstanceOf[AdapterView[_ <: Adapter]]).getOrElse {
        throw new RuntimeException("The content layout must have an AdapterView (e.g. ListView) whose id attribute is " + listViewName)
      }
      emptyViewOpt.map(adapterView.setEmptyView(_))
      adapterView.setOnItemClickListener(mOnClickListener)
      runOnUiThread(mRequestFocus)
    }
  }

  override def onRestoreInstanceState(savedInstanceState: Bundle) {
    crudContext.withExceptionReporting {
      if (currentCrudOperationType == CrudOperationType.List) {
        ensureAdapterView()
      }
      // This is before the super call to be the opposite order as onSaveInstanceState.
      crudContext.onRestoreActivityState(savedInstanceState)
    }
    super.onRestoreInstanceState(savedInstanceState)
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    crudContext.withExceptionReporting {
      // This is after the super call so that outState can be overridden if needed.
      crudContext.onSaveActivityState(outState)
    }
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

  /**
   * Get the activity's list view widget.
   */
  def getAdapterView: AdapterView[_ <: Adapter] = {
    ensureAdapterView()
    adapterView
  }

  protected lazy val contextMenuActions: Seq[Action] = {
    val actions = crudApplication.actionsFromCrudOperation(CrudOperation(entityName, CrudOperationType.Read))
    // Include the first action based on Android Feel even though available by just tapping.
    actions.filter(_.command.title.isDefined)
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo)
    crudContext.withExceptionReporting {
      val commands = contextMenuActions.map(_.command)
      for ((command, index) <- commands.zip(Stream.from(0)))
        menu.add(0, command.commandNumber, index, command.title.get)
    }
  }

  override def onContextItemSelected(item: MenuItem) = {
    crudContext.withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = contextMenuActions
      val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) => action.invoke(uriWithId(info.id), crudContext); true
        case None => super.onContextItemSelected(item)
      }
    }
  }

  def onListItemClick(l: AdapterView[_ <: Adapter], v: View, position: Int, id: ID) {
    crudContext.withExceptionReporting {
      if (id >= 0) {
        crudApplication.actionsFromCrudOperation(CrudOperation(entityName, CrudOperationType.Read)).headOption.map(_.invoke(uriWithId(id), crudContext)).getOrElse {
          warn("There are no entity actions defined for " + entityType)
        }
      } else {
        debug("Ignoring " + entityType + ".onListItemClick(" + id + ")")
      }
    }
  }

  override def onBackPressed() {
    crudContext.withExceptionReporting {
      if (currentCrudOperationType == CrudOperationType.Create || currentCrudOperationType == CrudOperationType.Update) {
        // Save before going back so that the Activity being activated will read the correct data from persistence.
        val createId = crudApplication.saveIfValid(this, entityType, contextItemsWithoutUseDefaults)
        val idOpt = entityType.IdField(currentUriPath)
        if (idOpt.isEmpty) {
          createId.foreach(id => createdId.set(Some(id)))
        }
      }
    }
    super.onBackPressed()
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    crudContext.withExceptionReporting {
      if (resultCode == Activity.RESULT_OK) {
        //"this" is included in the list so that existing data isn't cleared.
        entityType.copy(GetterInput(OperationResponse(requestCode, data), crudContext, this), this)
      } else {
        debug("onActivityResult received resultCode of " + resultCode + " and data " + data + " for request " + requestCode)
      }
    }
  }
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

  final def setListAdapterUsingUri(crudContext: AndroidCrudContext, activity: CrudActivity) {
    setListAdapter(activity.getAdapterView, entityType, crudContext, activity.contextItems, activity, rowLayout)
  }

  def setListAdapter[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, crudContext: AndroidCrudContext, contextItems: CrudContextItems, activity: Activity, itemLayout: LayoutKey) {
    val entityTypePersistedInfo = EntityTypePersistedInfo(entityType)
    val uri = toUri(contextItems.currentUriPath, crudContext.persistenceFactoryMapping)
    val adapter = new EntityCursorAdapter(entityType, contextItems, new ViewInflater(itemLayout, activity.getLayoutInflater), null)
    adapterView.setAdapter(adapter.asInstanceOf[A])
    cursorLoaderDataList.append(CursorLoaderData(ContentQuery(uri, entityTypePersistedInfo.queryFieldNames), adapter))
    getSupportLoaderManager.initLoader(cursorLoaderDataList.size - 1, null, this)
  }

  def onCreateLoader(id: Int, args: Bundle) = {
    val cursorLoaderData = cursorLoaderDataList(id)
    val contentQuery: ContentQuery = cursorLoaderData.query
    val loader = new CursorLoader(this)
    loader.setUri(contentQuery.uri)
    loader.setProjection(contentQuery.projection.toArray)
    loader.setSelection(contentQuery.selection.mkString(" AND "))
    loader.setSelectionArgs(contentQuery.selectionArgs.toArray)
    loader.setSortOrder(contentQuery.sortOrder.getOrElse(null))
    cursorLoaderDataByLoader.put(loader, cursorLoaderData)
    loader
  }

  def onLoaderReset(loader: Loader[Cursor]) {
    crudApplication.FuturePortableValueCache.get(stateHolder).clear()
  }

  def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
    crudApplication.FuturePortableValueCache.get(stateHolder).clear()
    cursorLoaderDataByLoader.get(loader).foreach { cursorLoaderData =>
      cursorLoaderData.adapter.swapCursor(data)
    }
  }

  def waitForWorkInProgress() {
    crudContext.waitForWorkInProgress()
  }

  override val toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}

case class CursorLoaderData(query: ContentQuery, adapter: EntityCursorAdapter)
