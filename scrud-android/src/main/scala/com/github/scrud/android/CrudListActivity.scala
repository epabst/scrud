package com.github.scrud.android

import android.widget.ListView
import android.app.ListActivity
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
class CrudListActivity extends ListActivity with BaseCrudActivity { self =>

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    crudContext.withExceptionReporting {
      setContentView(listLayout)

      val view = getListView
  		view.setHeaderDividersEnabled(true)
  		view.addHeaderView(getLayoutInflater.inflate(headerLayout, null))
      bindNormalActionsToViews()
      registerForContextMenu(getListView)

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
        menu.add(0, command.commandId, index, command.title.get)
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

  override def onListItemClick(l: ListView, v: View, position: Int, id: ID) {
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
