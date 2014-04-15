package com.github.scrud.copy

import com.github.scrud.context.CommandContext

/**
 * A field that can be get a value out of a source.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
trait SourceField[+V] {
  /** Get some value or None from the given source. */
  //todo add sourceUri
  def findValue(source: AnyRef, context: CommandContext): Option[V]

  /**
   * Get some value or None from the given source.
   * This should never be removed because it flags situations where an Option is passed in
   * when one never should be.
   */
  @deprecated("use findValue(sourceOpt.get, context)")
  final def findValue(sourceOpt: Option[AnyRef], context: CommandContext): Option[V] =
    sourceOpt.flatMap { findValue(_, context) }
}
