package com.github.scrud.persistence

import com.github.scrud.state.DestroyStateListener
import com.github.scrud.util.{DelegatingListenerSet, ListenerSet}
import com.github.scrud.EntityName

/** A CrudPersistence that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  * @see DerivedPersistenceFactory
  * @param delegateEntityNames used to be notified when they change.
  */
abstract class DerivedCrudPersistence[E <: AnyRef](val persistenceConnection: PersistenceConnection,
                                                   protected val listenerSet: ListenerSet[DataListener],
                                                   delegateEntityNames: EntityName*)
        extends SeqCrudPersistence[E] with ReadOnlyPersistence with DelegatingListenerSet[DataListener] {
  {
    val sharedContext = persistenceConnection.sharedContext
    val listenerForDelegateChanges = NotifyDataListenerSetListener(listenerSet)
    delegateEntityNames.foreach { delegateEntityName =>
      sharedContext.dataListenerHolder(delegateEntityName).addListenerIfNotPresent(listenerForDelegateChanges)
    }
    sharedContext.applicationState.addListener(new DestroyStateListener {
      def onDestroyState() {
        delegateEntityNames.foreach { delegateEntityName =>
          sharedContext.dataListenerHolder(delegateEntityName).removeListener(listenerForDelegateChanges)
        }
      }
    })
  }

  val sharedContext = persistenceConnection.sharedContext
}
