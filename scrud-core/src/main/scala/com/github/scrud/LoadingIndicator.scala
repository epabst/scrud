package com.github.scrud

import com.github.scrud.copy._
import scala.Some

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
  @deprecated("use LoadingIndicator(value) instead.", since = "2014-04-25")
  def loadingIndicator[T](value: => T): LoadingIndicator[T] = LoadingIndicator(value)
}

/**
 * Defines a field value that should be used when the real data isn't ready to display yet.
 * It is activated by copying from [[com.github.scrud.LoadingIndicator]].
 */
case class LoadingIndicator[T](loadingValue: T) extends SourceField[T] with AdaptableFieldConvertible[T] with RepresentationByType[T] {
  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this as needed. */
  val toPlatformIndependentFieldApplicability = FieldApplicability(Set(LoadingIndicator), Set.empty)

  private val someLoadingValue = Some(loadingValue)

  /** Get some value or None from the given source. */
  def findValue(source: AnyRef, context: CopyContext) = someLoadingValue

  /**
   * Converts this to an [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  val toAdaptableField = AdaptableField[T](Seq(LoadingIndicator -> this), Seq.empty)
}

object LoadingIndicator extends SourceType
