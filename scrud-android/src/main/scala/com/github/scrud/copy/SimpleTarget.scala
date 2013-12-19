package com.github.scrud.copy

/**
 * A place where data can be copied to.
 * Some examples are a UI page, a copy model entity, or a row in a database.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:16 PM
 */
class SimpleTarget[D <: AnyRef](val data: D)(implicit val dataManifest: Manifest[D]) extends Target[D]
