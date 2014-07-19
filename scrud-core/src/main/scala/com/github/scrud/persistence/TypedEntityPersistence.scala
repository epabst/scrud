package com.github.scrud.persistence

import com.github.scrud.UriPath

/** EntityPersistence with a specific type.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait TypedEntityPersistence[E <: AnyRef] extends EntityPersistence {
  override def findAll(uri: UriPath): Seq[E]

  override def find(uri: UriPath): Option[E] = super.find(uri).map(_.asInstanceOf[E])

  override def newWritable(): E
}



