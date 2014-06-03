package com.github.scrud.copy

/**
 * A SourceField that has a SourceField that operates on only part of the source.
 * @author epabst@gmail.com on 5/15/14.
 */
class NestedSourceField[V](partSpecifier: SourceField[AnyRef], nestedField: SourceField[V]) extends SourceField[V] {
  /** Get some value or None from the given source. */
  override def findValue(source: AnyRef, context: CopyContext): Option[V] = {
    val nestedSourceOpt = partSpecifier.findValue(source, context)
    nestedSourceOpt.flatMap(nestedField.findValue(_, context))
  }

  override lazy val toString = "NestedSourceField(" + partSpecifier + ", " + nestedField + ")"
}
