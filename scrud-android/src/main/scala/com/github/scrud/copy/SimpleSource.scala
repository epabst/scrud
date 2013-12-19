package com.github.scrud.copy

/**
 * A place where data can be copied from.
 * Some examples are a serialize form, a row in a database, or a data model entity.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:16 PM
 */
class SimpleSource[D <: AnyRef](val data: D)(implicit val dataManifest: Manifest[D]) extends Source[D]
