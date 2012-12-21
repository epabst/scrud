package com.github.scrud.action

/**
 * The unique identifier for a Command.  Often the String will contain an EntityName as a substring.
 * The string is not intended for display.  It should be localized to a display string.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/20/12
 * Time: 6:20 PM
 * @see [[com.github.scrud.action.Action]]
 */
case class CommandId(idString: String)
