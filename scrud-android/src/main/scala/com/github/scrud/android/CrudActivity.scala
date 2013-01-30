package com.github.scrud.android

import action.OperationResponse
import android.os.Bundle
import com.github.triangle.{GetterInput, PortableField}
import android.content.Intent
import android.app.Activity
import state.CachedStateListener
import com.github.scrud.action.CrudOperationType
import com.github.scrud.platform.PlatformTypes
import android.widget.{Adapter, AdapterView, ListView}
import com.github.scrud.persistence.DataListener
import com.github.scrud.UriPath
import android.view.{MenuItem, View, ContextMenu}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import com.github.scrud.platform.PlatformTypes._
import scala.Some
import com.github.scrud.action.Action
import com.github.scrud.action.CrudOperation

/** A generic Activity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudActivity extends BaseCrudActivity { self =>
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
            def onChanged(uri: UriPath) {
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
    if (currentCrudOperationType == CrudOperationType.List) {
      ensureAdapterView()
    }
    super.onRestoreInstanceState(savedInstanceState)
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
}
