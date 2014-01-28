package com.github.scrud.context

import com.github.scrud.UriPath
import com.github.scrud.state.State

/**
 * The context that is shared among all [[com.github.scrud.context.RequestContext]]s.
 * Some examples are:<ul>
 *   <li>A Servlet Context</li>
 *   <li>A running Android Application</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
abstract class SharedContext {
  def baseUri: UriPath

  def entityRegistry: EntityRegistry

  private[context] val state: State = new State {}
}
