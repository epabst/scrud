package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath
import com.github.scrud.copy.SourceType
import com.github.scrud.context.CommandContext

/**
 * A CrudPersistence that contains a specific type.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
trait TypedCrudPersistence[E <: AnyRef] extends TypedEntityPersistence[E] with CrudPersistence {
  /** Find an entity with a given ID using a baseUri. */
  override def find(id: ID, baseUri: UriPath): Option[E] = super.find(id, baseUri).map(_.asInstanceOf[E])

  override def find(uri: UriPath): Option[E] = super.find(uri).map(_.asInstanceOf[E])

  override def toWritable(sourceType: SourceType, source: AnyRef, sourceUri: UriPath, commandContext: CommandContext): E = {
    super.toWritable(sourceType, source, sourceUri, commandContext).asInstanceOf[E]
  }
}
