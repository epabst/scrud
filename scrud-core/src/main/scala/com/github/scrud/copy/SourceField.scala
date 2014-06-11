package com.github.scrud.copy

/**
 * A field that can be get a value out of a source.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
trait SourceField[+V] {
  /** Get some value or None from the given source. */
  def findValue(source: AnyRef, context: CopyContext): Option[V]

  /**
   * Get some value or None from the given source.
   * NEVER REMOVE THIS because it flags situations where an Option is passed in
   * when one never should be.
   * It's signature should always match the normal findValue method except for accepting an Option.
   */
  @deprecated("use findValue(sourceOpt.get, context)", since = "2014-05-08")
  final def findValue(sourceOpt: Option[AnyRef], context: CopyContext): Option[V] =
    sourceOpt.flatMap { findValue(_, context) }
}
