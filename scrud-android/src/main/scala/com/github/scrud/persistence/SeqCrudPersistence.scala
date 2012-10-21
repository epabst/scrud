package com.github.scrud.persistence

/**
 * A CrudPersistence stored in a Seq.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
trait SeqCrudPersistence[T <: AnyRef] extends SeqEntityPersistence[T] with CrudPersistence
