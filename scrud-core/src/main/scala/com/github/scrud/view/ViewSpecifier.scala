package com.github.scrud.view

import com.netaporter.uri.Uri
import com.github.scrud.action.Command
import com.github.scrud.EntityUriHolder

/**
 * Which view should be rendered (in the MVC pattern).  It is usable as the key in a view cache.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 9:30 AM
 * @param uri the resource that identifies the view data (which still needs to be fetched)
 * @param extraAvailableCommands extra commands, beyond the usual ones for the view, that the user may invoke such as "undo".
 * @param requestHeaders arbitrary headers provided by the caller, including any headers in the command that returned this ViewRequest
 * @see [[com.github.scrud.view.ViewRequest]]
 */
case class ViewSpecifier(uri: Uri, extraAvailableCommands: Seq[Command] = Seq.empty,
                         requestHeaders: Map[String, String] = Map.empty) extends EntityUriHolder
