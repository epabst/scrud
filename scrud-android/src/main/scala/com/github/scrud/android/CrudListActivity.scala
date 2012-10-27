package com.github.scrud.android

import action.Action
import android.widget.ListView
import android.app.ListActivity
import android.os.Bundle
import android.view.{ContextMenu, View, MenuItem}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import com.github.scrud.UriPath
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.persistence.DataListener

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

  protected def contextMenuActions: Seq[Action] = crudApplication.actionsForEntity(entityType) match {
    case _ :: tail => tail.filter(_.command.title.isDefined)
    case Nil => Nil
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
        case Some(action) => action.invoke(uriWithId(info.id), this); true
        case None => super.onContextItemSelected(item)
      }
    }
  }

  protected lazy val normalActions = crudApplication.actionsForList(entityType)

  override def onListItemClick(l: ListView, v: View, position: Int, id: ID) {
    crudContext.withExceptionReporting {
      if (id >= 0) {
        crudApplication.actionsForEntity(entityType).headOption.map(_.invoke(uriWithId(id), this)).getOrElse {
          warn("There are no entity actions defined for " + entityType)
        }
      } else {
        debug("Ignoring " + entityType + ".onListItemClick(" + id + ")")
      }
    }
  }
}
