package com.github.scrud

import com.github.triangle.Field
import com.github.scrud.platform.PlatformTypes._

/**
 * The name of an EntityType.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 4:42 PM
 */
case class EntityName(name: String) {
  override val toString = name

  object UriPathId extends Field[ID](UriPath.uriIdField(this))
}
