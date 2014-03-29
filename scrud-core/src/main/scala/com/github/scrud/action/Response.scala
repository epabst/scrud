package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.copy.{SourceWithType, SourceType}

/**
 * The response to an [[com.github.scrud.action.Action]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
case class Response(uri: Uri, sourceWithType: SourceWithType, headers: Map[String,String], availableCommands: Seq[Command])
