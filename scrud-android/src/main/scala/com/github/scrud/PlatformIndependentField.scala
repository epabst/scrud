package com.github.scrud

import com.github.triangle.{SingleGetter, PortableField}

/**
 * A factory for fields that are platform-independent.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:36 PM
 */
object PlatformIndependentField {

  /**
   * Defines a field value that should be used when the real data isn't ready to display yet.
   * It is activated by copying from [[com.github.scrud.LoadingIndicator]].
   */
  def loadingIndicator[T](value: => T): PortableField[T] = new SingleGetter[T]({
    case LoadingIndicator =>
      Some(value)
  }) {
    override val toString = "loadingValue(" + value + ")"
  }
}

object LoadingIndicator
