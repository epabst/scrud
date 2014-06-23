package com.github.scrud.persistence

/**
 * A CrudPersistence that is read-only.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/20/14
 */
trait ReadOnlyCrudPersistence extends ReadOnlyPersistence with CrudPersistence
