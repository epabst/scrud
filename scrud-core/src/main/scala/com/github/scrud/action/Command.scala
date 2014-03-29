package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.copy.SourceType

/**
 * An available command to perform an [[com.github.scrud.action.Action]] including parameterized state.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
case class Command(actionKey: ActionKey, uri: Uri, sourceType: SourceType, source: AnyRef, headers: Map[String,String])
