package com.github.scrud.android

import android.widget.{ListView, Adapter, AdapterView}
import android.os.Bundle
import android.view.{ContextMenu, View, MenuItem}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import com.github.scrud.UriPath
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.persistence.DataListener
import com.github.scrud.action.{CrudOperationType, CrudOperation, Action}

/** A generic ListActivity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudListActivity extends BaseCrudActivity { self =>
  private val mRequestFocus: Runnable = new Runnable {
    def run() {
      adapterView.focusableViewAvailable(adapterView)
    }
  }
  protected val mOnClickListener: AdapterView.OnItemClickListener = new AdapterView.OnItemClickListener {
    def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
      onListItemClick(parent.asInstanceOf[AdapterView[_ <: Adapter]], v, position, id)
    }
  }

  /**
   * Get the activity's list view widget.
   */
  def getAdapterView: AdapterView[_ <: Adapter] = {
    ensureAdapterView()
    adapterView
  }

  private var adapterView: AdapterView[_ <: Adapter] = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    crudContext.withExceptionReporting {
      ensureAdapterView()

  		getAdapterView match {
        case listView: ListView =>
          listView.setHeaderDividersEnabled(true)
          listView.addHeaderView(getLayoutInflater.inflate(headerLayout, null))
        case _ =>
      }
      bindNormalActionsToViews()
      registerForContextMenu(getAdapterView)

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
    }
  }

  /** This is taken from android's ListActivity.  Not sure what situations make it necessary. */
  private def ensureAdapterView() {
    if (adapterView == null) {
      setContentView(listLayout)
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


  override def onRestoreInstanceState(savedInstanceState: Bundle) {
    ensureAdapterView()
    super.onRestoreInstanceState(savedInstanceState)
  }

  /**
   * Updates the screen state (current list and other views) when the
   * content changes.
   *
   * @see Activity#onContentChanged()
   */
  override def onContentChanged() {
    super.onContentChanged()
    val emptyViewOpt: Option[View] = emptyListViewKeyOpt.flatMap(key => Option(findViewById(key)))
    this.adapterView = Option(findViewById(listViewKey).asInstanceOf[AdapterView[_ <: Adapter]]).getOrElse {
      throw new RuntimeException("The content layout must have an AdapterView (e.g. ListView) whose id attribute is " + listViewName)
    }
    emptyViewOpt.map(adapterView.setEmptyView(_))
    adapterView.setOnItemClickListener(mOnClickListener)
    runOnUiThread(mRequestFocus)
  }

  protected lazy val contextMenuActions: Seq[Action] = {
    val actions = crudApplication.actionsFromCrudOperation(CrudOperation(entityName, CrudOperationType.Read))
    actions match {
      case _ :: tail => tail.filter(_.command.title.isDefined)
      case Nil => Nil
    }
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
}
