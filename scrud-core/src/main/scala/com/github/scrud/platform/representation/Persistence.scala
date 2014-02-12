package com.github.scrud.platform.representation

import com.github.scrud.copy.StorageType

/**
 * A Persistence Representation and StorageType for copying to/from a given data version of Persistence.
 * When treated as a Representation, the data version is the minDataVersion, and there is no maxDataVersion.
 * @param dataVersion the version being copied to/from or the minDataVersion for a Representation
 * @return a Representation and StorageType
 * @see [[com.github.scrud.platform.representation.PersistenceRange]]
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
class Persistence(dataVersion: Int) extends PersistenceRange(dataVersion, Persistence.NoMaxVersion) with StorageType

object Persistence {
  val NoMaxVersion: Int = Int.MaxValue

  val Latest: Persistence = new Persistence(NoMaxVersion) {
    /** Gets the intrinsic FieldApplicability.  The PlatformDriver may replace this type needed. */
    override def toPlatformIndependentFieldApplicability =
      throw new UnsupportedOperationException("Specify a dataVersion (or range) when defining fields.  e.g. Persistence(dataVersion = 1)")
  }

  /**
   * Creates a Persistence Representation and StorageType for copying to/from a given data version of Persistence.
   * If there should be a maxDataVersion, use Persistence(Int,Int) instead.
   * @param dataVersion the data version that a field is represented in
   * @return a Representation
   */
  def apply(dataVersion: Int): Persistence = new Persistence(dataVersion)

  /**
   * Creates a Persistence Representation for the given min/max data versions.
   * If there shouldn't be a maxDataVersion, use [[com.github.scrud.platform.representation.Persistence.apply(Int)]] instead.
   * @param minDataVersion the minimum data version that a field is represented in
   * @param maxDataVersion the maximum (inclusive) data version that a field is represented in
   * @return a Representation
   */
  def apply(minDataVersion: Int, maxDataVersion: Int): PersistenceRange = PersistenceRange(minDataVersion, maxDataVersion)
}
