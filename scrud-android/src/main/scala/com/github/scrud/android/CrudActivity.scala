package com.github.scrud.android

import _root_.android.database.Cursor
import _root_.android.support.v4.app.{LoaderManager, FragmentActivity}
import _root_.android.support.v4.content.{CursorLoader, Loader}
import android.os.Bundle
import android.content.Intent
import android.app.Activity
import _root_.android.widget.{Adapter, AdapterView, ListView}
import com.github.scrud
import scrud.action.CrudOperationType
import scrud.platform.PlatformTypes
import scrud.persistence.{PersistenceFactory, DataListener}
import com.github.scrud.android.action.{AndroidOperation, ActivityResult, OptionsMenuActivity}
import scrud.android.state.CachedStateListener
import android.view.{MenuItem, View, ContextMenu}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import scrud.platform.PlatformTypes._
import java.util.concurrent.atomic.AtomicReference
import scrud.android.view.AndroidConversions._
import scrud.android.view.AndroidResourceAnalyzer._
import scrud.android.view._
import scrud.{android=>_,_}
import scala.collection.mutable
import com.github.scrud.android.persistence.EntityTypePersistedInfo
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.mapAsScalaConcurrentMap
import scala.collection.concurrent
import com.github.scrud.platform.representation.{DetailUI, SummaryUI, EditUI}
import com.github.scrud.copy._
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.android.persistence.ContentQuery
import com.github.scrud.action.CrudOperation
import com.github.scrud.action.OperationAction
import com.github.scrud.android.view.OnClickSetterField
import com.github.scrud.context.SharedContextHolder
import com.github.scrud.state.State

/** A generic Activity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudActivity extends FragmentActivity with OptionsMenuActivity with LoaderManager.LoaderCallbacks[Cursor] with SharedContextHolder { self =>
  lazy val entityType: EntityType = entityTypeMap.allEntityTypes.find(entityType => Some(entityType.entityName) == UriPath.lastEntityNameOption(currentUriPath)).getOrElse {
    throw new IllegalStateException("No valid entityName in " + currentUriPath)
  }

  override lazy val applicationState: State = super.applicationState

  def entityName = entityType.entityName

  protected lazy val persistenceFactory: PersistenceFactory = commandContext.entityTypeMap.persistenceFactory(entityType)

  protected[this] val createdId: AtomicReference[Option[ID]] = new AtomicReference(None)

  private[this] val cursorLoaderDataList = mutable.Buffer[CursorLoaderData]()

  private[this] val cursorLoaderDataByLoader: concurrent.Map[Loader[Cursor],CursorLoaderData] = new ConcurrentHashMap[Loader[Cursor],CursorLoaderData]()

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  protected lazy val initialUriPath: UriPath = {
    // The primary EntityType is used as the default starting point.
    val defaultContentUri = toUriPath(baseUriFor(sharedContext)) / sharedContext.entityNavigation.primaryEntityType.entityName
    // If no data was given in the intent (e.g. because we were started as a MAIN activity),
    // then use our default content provider.
    Option(getIntent).flatMap(intent => Option(intent.getData).map(toUriPath(_))).getOrElse(defaultContentUri)
  }

  /** not a lazy val since dynamic in that CrudActivity.saveBasedOnUserAction sets the ID. */
  def currentUriPath: UriPath = createdId.get().fold(initialUriPath)(initialUriPath / _)

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

  lazy val currentUITargetType: TargetType = currentCrudOperationType match {
    case CrudOperationType.Update | CrudOperationType.Create =>
      EditUI
    case CrudOperationType.List =>
      SummaryUI
    case CrudOperationType.Read | CrudOperationType.Delete =>
      DetailUI
  }

  def uriWithId(id: ID): UriPath = UriPath.specify(currentUriPath, entityName, id)

  //final since only here as a convenience method.
  final def stateHolder = commandContext.stateHolder

  lazy val sharedContext: CrudAndroidApplicationLike = getApplication.asInstanceOf[CrudAndroidApplicationLike]

  /** A convenient alias for [[sharedContext]]. */
  def androidApplication: CrudAndroidApplicationLike = sharedContext

  override lazy val platformDriver: AndroidPlatformDriver = super.platformDriver.asInstanceOf[AndroidPlatformDriver]

  def entityNavigation = sharedContext.entityNavigation

  lazy val commandContext: AndroidCommandContext = new AndroidCommandContext(this, sharedContext)

  protected lazy val normalActions = entityNavigation.actionsFromCrudOperation(currentCrudOperation)

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

    commandContext.withExceptionReporting {
      setContentViewForOperation()
      populateDataInViews()
      bindActionsToViews()
      if (entityTypeMap.maySpecifyEntityInstance(currentUriPath, entityType)) {
        commandContext.addCachedActivityStateListener(new CachedStateListener {
          def onClearState(stayActive: Boolean) {
            if (stayActive) {
              populateFromUri(entityType, currentUriPath, currentUITargetType)
            }
          }

          def onSaveState(outState: Bundle) {
            entityType.copyAndUpdate(EditUI, this, currentUriPath, BundleStorage, outState, commandContext)
          }

          def onRestoreState(savedInstanceState: Bundle) {
            val adaptedValueSeq = entityType.copy(BundleStorage, savedInstanceState, currentUriPath, currentUITargetType, commandContext)
            populateFromValues(adaptedValueSeq)
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
        setListAdapterUsingUri(commandContext, this)
        commandContext.future {
          populateFromReferencedEntities()
          persistenceFactory.addListener(new DataListener {
            def onChanged() {
              //Some of the parent fields may be calculated from the children
              populateFromReferencedEntities()
            }
          }, entityType, sharedContext)
        }
      case _ =>
        val uri = currentUriPath
        commandContext.future {
          if (entityTypeMap.maySpecifyEntityInstance(uri, entityType)) {
            populateFromUri(entityType, uri, currentUITargetType)
          } else {
            val defaultValues = commandContext.findDefault(entityType, uri, currentUITargetType)
            populateFromValues(defaultValues)
          }
        }
    }
  }

  private[scrud] def populateFromReferencedEntities() {
    val uriPath = currentUriPath
    //copy each referenced Entity's data to the Activity if identified in the currentUriPath
    entityType.referencedEntityNames.foreach { referencedEntityName =>
      val referencedEntityType = entityTypeMap.entityType(referencedEntityName)
      if (entityTypeMap.maySpecifyEntityInstance(uriPath, referencedEntityType)) {
        populateFromUri(referencedEntityType, uriPath, currentUITargetType)
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
    commandContext.withExceptionReporting {
      if (currentCrudOperationType == CrudOperationType.List) {
        ensureAdapterView()
      }
      // This is before the super call to be the opposite order as onSaveInstanceState.
      commandContext.onRestoreActivityState(savedInstanceState)
    }
    super.onRestoreInstanceState(savedInstanceState)
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    commandContext.withExceptionReporting {
      // This is after the super call so that outState can be overridden if needed.
      commandContext.onSaveActivityState(outState)
    }
  }

  override def onResume() {
    commandContext.withExceptionReporting {
      trace("onResume")
      commandContext.onClearActivityState(stayActive = true)
    }
    super.onResume()
  }

  override def onDestroy() {
    commandContext.withExceptionReporting {
      commandContext.activityState.onDestroyState()
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

  protected lazy val contextMenuActions: Seq[OperationAction] = {
    val actions = entityNavigation.actionsFromCrudOperation(CrudOperation(entityName, CrudOperationType.Read))
    // Include the first action based on Android Feel even though available by just tapping.
    actions.filter(_.command.title.isDefined)
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo)
    commandContext.withExceptionReporting {
      val commands = contextMenuActions.map(_.command)
      for ((command, index) <- commands.zip(Stream.from(0)))
        menu.add(0, command.commandNumber, index, command.title.get)
    }
  }

  override def onContextItemSelected(item: MenuItem) = {
    commandContext.withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = contextMenuActions
      val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) => commandContext.future { action.invoke(uriWithId(info.id), commandContext) }; true
        case None => super.onContextItemSelected(item)
      }
    }
  }

  def onListItemClick(l: AdapterView[_ <: Adapter], v: View, position: Int, id: ID) {
    commandContext.withExceptionReporting {
      if (id >= 0) {
        entityNavigation.actionsFromCrudOperation(CrudOperation(entityName, CrudOperationType.Read)).headOption.
          fold(warn("There are no entity actions defined for " + entityType))(_.invoke(uriWithId(id), commandContext))
      } else {
        debug("Ignoring " + entityType + ".onListItemClick(" + id + ")")
      }
    }
  }

  override def onBackPressed() {
    commandContext.withExceptionReporting {
      if (currentCrudOperationType == CrudOperationType.Create || currentCrudOperationType == CrudOperationType.Update) {
        commandContext.future {
          val createId = commandContext.saveIfValid(currentUriPath, EditUI, this, entityType)
          val idOpt = entityType.idField.findFromContext(entityType.toUri(createId), commandContext)
          if (idOpt.isEmpty) {
            createId.foreach(id => createdId.set(Some(id)))
          }
        }
      }
    }
    super.onBackPressed()
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    commandContext.withExceptionReporting {
      if (resultCode == Activity.RESULT_OK) {
        // Only fields that can use a SourceType ActivityResult(ViewRef(requestCode)) will use this.
        entityType.copyAndUpdate(ActivityResult(ViewRef(requestCode)), data, currentUriPath, currentUITargetType, this, commandContext)
      } else {
        debug("onActivityResult received resultCode of " + resultCode + " and data " + data + " for request " + requestCode)
      }
    }
  }

  def populateFromUri(entityType: EntityType, uri: UriPath, targetType: TargetType) {
    commandContext.populateFromUri(entityType, uri, targetType, this)
  }

  def populateFromValues(adaptedValueSeq: AdaptedValueSeq) {
    commandContext.populateFromValueSeq(adaptedValueSeq, this)
  }

  lazy val entityNameLayoutPrefix = entityName.toSnakeCase

  protected def getLayoutKey(layoutName: String): LayoutKey = platformDriver.getLayoutKey(layoutName)

  lazy val headerLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_header")
  lazy val listLayout: LayoutKey = findResourceIdWithName(platformDriver.rLayoutClasses, entityNameLayoutPrefix + "_list").getOrElse(getLayoutKey("entity_list"))

  protected def listViewName: String = entityName + "_list"
  lazy val listViewKey: ViewKey = resourceIdWithName(sharedContext.rIdClasses, listViewName, "id")
  lazy val emptyListViewKeyOpt: Option[ViewKey] = findResourceIdWithName(sharedContext.rIdClasses, entityName + "_emptyList")

  lazy val rowLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_row")
  /** The layout used for each entity when allowing the user to pick one of them. */
  lazy val pickLayout: LayoutKey = pickLayoutFor(entityName)
  lazy val entryLayout: LayoutKey = getLayoutKey(entityNameLayoutPrefix + "_entry")

  /** The layout used for each entity when allowing the user to pick one of them. */
  def pickLayoutFor(entityName: EntityName): LayoutKey = platformDriver.selectUILayoutFor(entityName)

  // not a val because it is dynamic
  protected def applicableActions: List[OperationAction] = LastUndoable.get(commandContext.stateHolder).map(_.undoAction).toList ++ normalActions

  protected lazy val nestedNormalOperationSetters: Seq[NestedTargetField[Nothing]] = {
    val setters = normalActions.map { action =>
      val viewRef = ViewRef(action.command.commandId.toCamelCase + "_command", sharedContext.rIdClasses)
      new OnClickSetterField(_ => action.operation).forTargetView(ViewSpecifier(viewRef))
    }
    setters
  }

  protected def bindNormalActionsToViews() {
    val context = new CopyContext(currentUriPath, commandContext)
    nestedNormalOperationSetters.foreach(_.updateValue(this, None, context))
  }

  private[android] def onCommandsChanged() {
    optionsMenuCommands = generateOptionsMenu.map(_.command)
  }

  // not a val because it is dynamic
  protected def generateOptionsMenu: List[OperationAction] =
    applicableActions.filter(action => action.command.title.isDefined || action.command.icon.isDefined)

  // not a val because it is dynamic
  def defaultOptionsMenuCommands = generateOptionsMenu.map(_.command)

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    commandContext.withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = generateOptionsMenu
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) =>
          action.invoke(currentUriPath, commandContext)
          if (LastUndoable.get(commandContext.stateHolder).exists(_.undoAction.commandId == item.getItemId)) {
            LastUndoable.clear(commandContext.stateHolder)
            onCommandsChanged()
          }
          true
        case None => super.onOptionsItemSelected(item)
      }
    }
  }

  final def setListAdapterUsingUri(commandContext: AndroidCommandContext, activity: CrudActivity) {
    setListAdapter(activity.getAdapterView, entityType, activity.currentUriPath, activity.currentUITargetType, activity, rowLayout)
  }

  def setListAdapter[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, uri: UriPath, targetType: TargetType, activity: Activity, itemLayout: LayoutKey) {
    val entityTypePersistedInfo = new EntityTypePersistedInfo(entityType)
    val androidUri = toUri(uri, sharedContext)
    val adapter = new EntityCursorAdapter(entityType, uri, targetType, commandContext, new ViewInflater(itemLayout, activity.getLayoutInflater), null)
    val cursorLoaderData = CursorLoaderData(ContentQuery(androidUri, entityTypePersistedInfo.queryFieldNames), adapter)
    runOnUiThread {
      adapterView.setAdapter(adapter.asInstanceOf[A])
      cursorLoaderDataList.append(cursorLoaderData)
      Option(getSupportLoaderManager).foreach(_.initLoader(cursorLoaderDataList.size - 1, null, this))
    }
  }

  def onCreateLoader(id: Int, args: Bundle) = {
    commandContext.propagateWithExceptionReporting {
      debug("onCreateLoader(" + id + ")")
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
  }

  def onLoaderReset(loader: Loader[Cursor]) {
    commandContext.withExceptionReporting {
      debug("onLoaderReset(" + loader + ")")
      sharedContext.FuturePortableValueCache.get(stateHolder).clear()
    }
  }

  def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
    commandContext.withExceptionReporting {
      debug("onLoadFinished(" + loader + ", cursor with count=" + data.getCount + ")")
      sharedContext.FuturePortableValueCache.get(stateHolder).clear()
      cursorLoaderDataByLoader.get(loader).foreach { cursorLoaderData =>
        cursorLoaderData.adapter.swapCursor(data)
      }
    }
  }

  override val toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}

case class CursorLoaderData(query: ContentQuery, adapter: EntityCursorAdapter)
