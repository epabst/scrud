package com.github.scrud.util

/**
 * Something that represents a name of some sort.
 * This provides useful conversions for various formats.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/27/14
 *         Time: 11:16 PM
 */
trait Name {
  /** A name as a Java identifier such as PoliceOfficer or crescentWrench. */
  def name: String

  override def equals(p1: scala.Any): Boolean

  override def hashCode(): Int

  override val toString = name

  lazy val toDisplayableString: String = name.replaceAll("([a-z])([A-Z])", "$1 $2")
}
