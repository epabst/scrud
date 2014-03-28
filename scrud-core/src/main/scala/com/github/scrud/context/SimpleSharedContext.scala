package com.github.scrud.context

import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.PlatformDriver

/**
 * The context that is shared among all [[com.github.scrud.context.CommandContext]]s.
 * Some examples are:<ul>
 *   <li>A Servlet Context</li>
 *   <li>A running Android Application</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
class SimpleSharedContext(val entityTypeMap: EntityTypeMap, val platformDriver: PlatformDriver) extends SharedContext
