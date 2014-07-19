package com.github.scrud.persistence

/**
 * A TypedCrudPersistence that is read-only.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/20/14
 */
trait TypedReadOnlyCrudPersistence[E <: AnyRef] extends ReadOnlyCrudPersistence with TypedCrudPersistence[E]
