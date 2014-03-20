package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.copy.SourceType

/**
 * The request to perform an [[com.github.scrud.action.Action]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
case class Request(commandKey: CommandKey, uri: Uri, sourceType: SourceType, source: AnyRef, headers: Map[String,String])
