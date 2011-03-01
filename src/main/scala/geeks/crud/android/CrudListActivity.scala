package geeks.crud.android

import _root_.android.widget.{ListView, ListAdapter}
import android.content.Intent
import _root_.android.app.{Activity, AlertDialog, ListActivity}
import android.os.Bundle
import android.net.Uri
import android.view.{View, MenuItem, Menu}
import android.content.{Context, DialogInterface}
import geeks.crud._

/**
 * A generic ListActivity for CRUD operations
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/3/11
 * Time: 7:06 AM
 * @param Q the query criteria type
 * @param L the type of findAll (e.g. Cursor)
 * @param R the type to read from (e.g. Cursor)
 * @param W the type to write to (e.g. ContentValues)
 */
abstract class CrudListActivity[Q <: AnyRef,L <: AnyRef,R <: AnyRef,W <: AnyRef](val entityConfig: CrudEntityConfig[Q,L,R,W])
  extends ListActivity with CrudContext[Q,L,R,W] {

  type ID = Long
  val ADD_DIALOG_ID = 100
  val EDIT_DIALOG_ID = 101

  lazy val contentProviderAuthority = this.getClass.getPackage.toString
  lazy val defaultContentUri = Uri.parse("content://" + contentProviderAuthority + "/" + entityConfig.entityName);

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView(entityConfig.listLayout)

    // If no data was given in the intent (because we were started
    // as a MAIN activity), then use our default content provider.
    if (getIntent.getData() == null) getIntent.setData(defaultContentUri);

    val view = getListView();
		view.setHeaderDividersEnabled(true);
		view.addHeaderView(getLayoutInflater().inflate(entityConfig.headerLayout, null));

    setListAdapter(persistence.createListAdapter(this))
  }

  override def onResume() {
    super.onResume
    refreshAfterSave()
  }

  //todo add support for item actions on long touch on an item

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val listActions = entityConfig.getListActions(actionFactory)
    for (action <- listActions if (action.title.isDefined || action.icon.isDefined)) {
      val menuItem = if (action.title.isDefined) {
        menu.add(0, listActions.indexOf(action), listActions.indexOf(action), action.title.get)
      } else {
        menu.add(0, listActions.indexOf(action), listActions.indexOf(action), "")
      }
      action.icon.map(icon => menuItem.setIcon(icon))
    }
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean = {
    val listActions = entityConfig.getListActions(actionFactory)
    val action = listActions(item.getItemId)
    action.apply()
    true
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: ID) {
    entityConfig.getEntityActions(actionFactory, id).headOption.map(_())
  }

  def refreshAfterSave()

  /**
   * Creates an edit dialog in the given Context to edit the entity and save it.
   * @param entityToEdit an Entity instance to edit or None to add a new one
   */
  //todo replace this with a CrudActivity
  def createEditDialog(context: Activity, entityId: Option[ID], afterSave: () => Unit = () => {}): AlertDialog = {
    val builder = new AlertDialog.Builder(context)
    val entryView = context.getLayoutInflater.inflate(entityConfig.entryLayout, null)
    //Unit is used to set the default value if no entityId is provided
    val readable = entityId.map(persistence.find).getOrElse(Unit)
    entityConfig.copyFields(readable, entryView)
    builder.setView(entryView)
    builder.setTitle(if (entityId.isDefined) entityConfig.editItemString else entityConfig.addItemString)
    builder.setPositiveButton(if (entityId.isDefined) entityConfig.editItemString else entityConfig.addItemString, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        dialog.dismiss
        val writable = persistence.newWritable
        entityConfig.copyFields(entryView, writable)
        persistence.save(entityId, writable)
        afterSave()
      }
    })
    builder.setNegativeButton(entityConfig.cancelItemString, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        dialog.cancel
      }
    })
    builder.create
  }
}
