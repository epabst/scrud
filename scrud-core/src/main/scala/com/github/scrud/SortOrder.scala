package com.github.scrud

/**
 * An enumeration of sort orders for a field.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/14/14
 *         Time: 12:06 AM
 */
object SortOrder extends Enumeration {
  type SortOrder = Value
  val Ascending = Value("Ascending")
  val Descending = Value("Descending")
  val Unsorted = Value("Unsorted")
}
