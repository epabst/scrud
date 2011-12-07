package com.github.scala.android.crud

import persistence.{EntityPersistence, EntityType}
import android.widget.ListAdapter

/**
 * A factory for EntityPersistence specific to a storage type such as SQLite.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/6/11
 * Time: 10:05 PM
 */

trait PersistenceFactory {
  def newWritable: AnyRef

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext): EntityPersistence

  def setListAdapter(crudType: CrudType, findAllResult: Seq[AnyRef], contextItems: List[AnyRef], activity: CrudListActivity)

  def refreshAfterDataChanged(listAdapter: ListAdapter)
}
