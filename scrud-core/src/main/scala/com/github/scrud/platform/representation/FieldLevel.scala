package com.github.scrud.platform.representation

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/4/14
 */
object FieldLevel extends Enumeration {
  type FieldLevel = Value

  val Detail = Value
  val Summary = Value
  val Identity = Value

  @deprecated("use Identity", since = "2014-08-04")
  val Select = Identity
}
