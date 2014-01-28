package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import platform.PlatformTypes
import com.github.scrud.types.QualifiedType
import com.github.scrud.util.Name

/**
 * The name of an EntityType.  It is also a QualifiedType for an entity ID value.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 4:42 PM
 */
case class EntityName(name: String) extends QualifiedType[PlatformTypes.ID] with Name {
  def toUri(id: ID) = UriPath(this, id)
}
