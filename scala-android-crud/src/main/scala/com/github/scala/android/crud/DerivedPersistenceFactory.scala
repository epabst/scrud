package com.github.scala.android.crud

import common.{DelegatingListenerSet, ListenerSet, UriPath}
import persistence.{DataListener, ReadOnlyPersistence, EntityType}

/** A CrudPersistence that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  * @see DerivedPersistenceFactory
  */
abstract class DerivedCrudPersistence[T <: AnyRef](val crudContext: CrudContext,
                                                   protected val listenerSet: ListenerSet[DataListener],
                                                   delegates: EntityType*)
        extends SeqCrudPersistence[T] with ReadOnlyPersistence with DelegatingListenerSet[DataListener] {
  {
    val listenerForDelegateChanges = NotifyDataListenerSetListener(listenerSet)
    delegates.foreach { delegate =>
      crudContext.dataListenerHolder(delegate).addListenerIfNotPresent(listenerForDelegateChanges)
    }
    crudContext.applicationState.addListener(new DestroyStateListener {
      def onDestroyState() {
        delegates.foreach { delegate =>
          crudContext.dataListenerHolder(delegate).removeListener(listenerForDelegateChanges)
        }
      }
    })
  }

  val delegatePersistenceMap: Map[EntityType,CrudPersistence] =
    delegates.map(delegate => delegate -> crudContext.openEntityPersistence(delegate)).toMap

  override def close() {
    delegatePersistenceMap.values.foreach(_.close())
    super.close()
  }
}

/**
 * This is a top-level case class so that it can be identified correctly to avoid re-adding it and for removal.
 * It is identified by which ListenerSet it contains.
 */
case class NotifyDataListenerSetListener(listenerSet: ListenerSet[DataListener]) extends DataListener {
  def onChanged(uri: UriPath) {
    listenerSet.listeners.foreach(_.onChanged(uri))
  }
}

/** A PersistenceFactory that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class DerivedPersistenceFactory[T <: AnyRef](delegates: EntityType*) extends GeneratedPersistenceFactory[T] { self =>
  def findAll(entityType: EntityType, uri: UriPath, delegatePersistenceMap: Map[EntityType,CrudPersistence]): Seq[T]

  def createEntityPersistence(_entityType: EntityType, crudContext: CrudContext) = {
    new DerivedCrudPersistence[T](crudContext, listenerSet(_entityType, crudContext), delegates: _*) {
      def entityType = _entityType

      def findAll(uri: UriPath): Seq[T] = self.findAll(_entityType, uri, delegatePersistenceMap)
    }
  }
}
