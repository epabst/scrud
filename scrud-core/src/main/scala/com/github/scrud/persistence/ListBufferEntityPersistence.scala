package com.github.scrud.persistence

import com.github.scrud.util.ListenerSet
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{EntityName, UriPath}
import collection.mutable
import java.util.concurrent.atomic.AtomicLong

/**
 * An EntityPersistence stored in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 4:57 PM
 */
class ListBufferEntityPersistence[E <: AnyRef](entityName: EntityName, newWritableFunction: => E,
                                               listenerSet: ListenerSet[DataListener]) extends SeqEntityPersistence[E] {
  private case class IDAndEntity(id: ID, entity: E) {
    override def toString = id + " -> " + entity
  }

  private val buffer = mutable.ListBuffer[IDAndEntity]()

  // not a val since dynamic
  def listeners = listenerSet.listeners

  private val nextId = new AtomicLong(10000L)

  def toUri(id: ID) = entityName.toUri(id)

  def findAll(uri: UriPath): List[E] = rawFindAll(uri).map(_.entity)

  private def rawFindAll(uri: UriPath): List[IDAndEntity] = {
    uri.findId(entityName).map(id => buffer.toList.filter(item => item.id == id)).getOrElse(buffer.toList)
  }

  def newWritable() = newWritableFunction

  def doSave(idOpt: Option[ID], entity: AnyRef) = {
    val newId = idOpt.getOrElse {
      nextId.incrementAndGet()
    }
    val index = idOpt.map(id => buffer.indexWhere(_.id == id)).getOrElse(-1)
    index match {
      case -1 =>
        // Prepend so that the newest ones come out first in results
        buffer.prepend(IDAndEntity(newId, entity.asInstanceOf[E]))
      case _ =>
        buffer(index) = IDAndEntity(newId, entity.asInstanceOf[E])
    }
    newId
  }

  def doDelete(uri: UriPath): Int = {
    val matches = rawFindAll(uri)
    matches.foreach(idAndEntity => buffer -= idAndEntity)
    matches.size
  }

  def close() {}

  override def toString = super.toString + "[" + buffer.mkString(",") + "]"
}
