package com.github.scrud.android.common

/** An Iterator whose items are calculated lazily.
  * @author Eric Pabst (epabst@gmail.com)
  */
private[scrud] trait CalculatedIterator[T] extends BufferedIterator[T] {
  private var calculatedNextValue: Option[Option[T]] = None

  def calculateNextValue(): Option[T]

  private def determineNextValue(): Option[T] = {
    if (!calculatedNextValue.isDefined) {
      calculatedNextValue = Some(calculateNextValue())
    }
    calculatedNextValue.get
  }

  def hasNext = determineNextValue().isDefined

  def head = determineNextValue().get

  def next() = {
    val next = head
    calculatedNextValue = None
    next
  }
}
