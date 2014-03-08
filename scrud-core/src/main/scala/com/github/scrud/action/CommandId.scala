package com.github.scrud.action

import com.github.scrud.util.Name

/**
 * The unique identifier for a Command.  Often the String will contain an EntityName as a substring.
 * The string is not intended for display.  It should be localized to a display string.
 * @param name a name such as a Java identifier like PoliceOfficer (pascal case) or crescentWrench (camel case).
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/20/12
 * Time: 6:20 PM
 * @see [[com.github.scrud.action.Action]]
 */
case class CommandId(name: String) extends Name
