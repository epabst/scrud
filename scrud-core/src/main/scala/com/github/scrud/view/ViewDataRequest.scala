package com.github.scrud.view

import com.github.scrud.copy.SourceWithType
import com.github.scrud.EntityUriHolder
import com.github.scrud.action.Command
import com.netaporter.uri.Uri
import scala.util.Try

/**
 * A ViewSpecifier along with the data for the view to render.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 9:30 AM
 * @param viewSpecifier which view is requested
 * @param modelDataTry the data to render into the view, if any exists
 * @param viewHeaders headers about the view being rendered (such as caching directives)
 * @see [[com.github.scrud.view.ViewRequest]]
 */
case class ViewDataRequest(viewSpecifier: ViewSpecifier, modelDataTry: Try[SourceWithType],
                           viewHeaders: Map[String, String] = Map.empty) extends EntityUriHolder with ModelDataTryHolder {
  def uri: Uri = viewSpecifier.uri

  def extraAvailableCommands: Seq[Command] = viewSpecifier.extraAvailableCommands
}
