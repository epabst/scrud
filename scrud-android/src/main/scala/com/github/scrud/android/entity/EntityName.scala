package com.github.scrud.android.entity

import com.github.triangle.Field
import com.github.scrud.android.common.PlatformTypes._
import com.github.scrud.android.common.UriPath._

/**
 * The name of an EntityType.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 4:42 PM
 */
case class EntityName(name: String) {
  override def toString = name

  object UriPathId extends Field[ID](uriIdField(this))
}
