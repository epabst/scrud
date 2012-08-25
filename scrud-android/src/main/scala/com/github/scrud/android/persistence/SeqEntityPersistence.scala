package com.github.scrud.android.persistence

import scala.collection.mutable
import com.github.triangle.{Setter, Getter, Field}
import com.github.scrud.android.common.PlatformTypes._
import java.util.concurrent.atomic.AtomicLong
import com.github.scrud.android.common._

/** EntityPersistence for a simple generated Seq.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait SeqEntityPersistence[T <: AnyRef] extends EntityPersistence {
  def newWritable: T
}

trait ReadOnlyPersistence extends EntityPersistence {
  def newWritable = throw new UnsupportedOperationException("write not supported")

  def doSave(id: Option[ID], data: AnyRef): ID = throw new UnsupportedOperationException("write not supported")

  def doDelete(uri: UriPath) { throw new UnsupportedOperationException("delete not supported") }

  def close() {}
}

abstract class ListBufferEntityPersistence[T <: AnyRef](newWritableFunction: => T, listenerSet: ListenerSet[DataListener]) extends SeqEntityPersistence[T] {
  private object IdField extends Field[ID](Getter[IdPk,ID](_.id).withUpdater(e => e.id(_)) +
      Setter((e: MutableIdPk) => e.id = _) + CursorField.PersistedId)
  val buffer = mutable.ListBuffer[T]()

  def listeners = listenerSet.listeners

  val nextId = new AtomicLong(10000L)

  //todo only return the one that matches the ID in the uri, if present
  //def findAll(uri: UriPath) = buffer.toList.filter(item => uri.segments.containsSlice(toUri(IdField(item)).segments))
  def findAll(uri: UriPath) = buffer.toList

  def newWritable = newWritableFunction

  def doSave(id: Option[ID], item: AnyRef) = {
    val newId = id.getOrElse {
      nextId.incrementAndGet()
    }
    // Prepend so that the newest ones come out first in results
    buffer.prepend(IdField.updateWithValue(item.asInstanceOf[T], Some(newId)))
    newId
  }

  def doDelete(uri: UriPath) {
    findAll(uri).foreach(entity => buffer -= entity)
  }

  def close() {}
}
