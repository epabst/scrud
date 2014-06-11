package com.github.scrud.action

/**
 * A REST method.  Also known as an HTTP method.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 11:49 AM
 * @see [[com.github.scrud.action.ActionKey]]
 */
object RestMethod extends Enumeration {
  type RestMethod = Value
  val GET, DELETE, POST, PUT = Value
}
