package com.github.scrud.persistence

import com.github.scrud.state.DestroyStateListener
import com.github.scrud.util.{DelegatingListenerSet, ListenerSet}
import com.github.scrud.EntityName
import com.github.scrud.context.SharedContext

/** A CrudPersistence that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  * @see DerivedPersistenceFactory
  */
abstract class DerivedCrudPersistence[T <: AnyRef](val sharedContext: SharedContext,
                                                   protected val listenerSet: ListenerSet[DataListener],
                                                   delegates: EntityName*)
        extends SeqCrudPersistence[T] with ReadOnlyPersistence with DelegatingListenerSet[DataListener] {
  {
    val listenerForDelegateChanges = NotifyDataListenerSetListener(listenerSet)
    delegates.foreach { delegate =>
      sharedContext.dataListenerHolder(delegate).addListenerIfNotPresent(listenerForDelegateChanges)
    }
    sharedContext.applicationState.addListener(new DestroyStateListener {
      def onDestroyState() {
        delegates.foreach { delegate =>
          sharedContext.dataListenerHolder(delegate).removeListener(listenerForDelegateChanges)
        }
      }
    })
  }

  val delegatePersistenceMap: Map[EntityName,CrudPersistence] =
    delegates.map(delegate => delegate -> sharedContext.openEntityPersistence(delegate)).toMap

  override def close() {
    delegatePersistenceMap.values.foreach(_.close())
    super.close()
  }
}
