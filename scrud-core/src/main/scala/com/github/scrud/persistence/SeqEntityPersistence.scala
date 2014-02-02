package com.github.scrud.persistence

/** EntityPersistence for a simple generated Seq.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait SeqEntityPersistence[E <: AnyRef] extends EntityPersistence {
  def newWritable(): E
}



