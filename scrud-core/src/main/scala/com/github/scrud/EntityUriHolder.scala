package com.github.scrud

import com.netaporter.uri.Uri
import com.github.scrud.platform.PlatformTypes._

/**
 * Something that holds an entity Uri.  This provides some helpful functions.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 10:30 AM
 */
trait EntityUriHolder {
  def uri: Uri
  val entityNameOpt: Option[EntityName] = UriPath.lastEntityNameOption(uri)
  val entityNameOrFail: EntityName = entityNameOpt.getOrElse(sys.error("no EntityName found in uri=" + uri))
  val entityIdOpt: Option[ID] = entityNameOpt.flatMap(UriPath.findId(uri, _))
}
