package com.github.scrud.platform.representation

import com.github.scrud.copy.Representation

/**
 * A [[com.github.scrud.copy.Representation]] for a field that applies to a range of data versions.
 * @see [[com.github.scrud.platform.representation.Persistence]]
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/12/14
 *         Time: 8:00 AM
 */
case class PersistenceRange(minDataVersion: Int, maxDataVersion: Int) extends Representation[Nothing] {
  def includes(persistence: Persistence): Boolean =
    persistence.dataVersion >= minDataVersion && persistence.dataVersion <= maxDataVersion
}
