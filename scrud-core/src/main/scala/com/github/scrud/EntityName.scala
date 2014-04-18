package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.util.Name

/**
 * The name of an EntityType.  It is also a QualifiedType for an entity ID value.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 4:42 PM
 */
case class EntityName(name: String) extends QualifiedTypeProvidingFieldName[ID] with Name {
  def toUri(id: ID) = UriPath(this, id)

  def toUri(idOpt: Option[ID]) = idOpt.map(UriPath(this, _)).getOrElse(UriPath(this))

  /** The field name does not include "ID" to make it more intuitive when displaying and referencing. */
  override def toFieldName = toCamelCase
}
