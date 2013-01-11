package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidResourceAnalyzer._

/**
 * A reference to an element in the UI.  It wraps a [[com.github.scrud.platform.PlatformTypes.ViewKey]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/26/12
 * Time: 6:39 AM
 */

trait ViewRef {
  def viewKeyOpt: Option[ViewKey]

  def viewKeyOrError: ViewKey

  /**
   * The name of the field as a String.
   * @param rIdClasses a list of R.id classes that may contain the id.
   * @throws IllegalStateException if it cannot be determined
   */
  def fieldName(rIdClasses: Seq[Class[_]]): String

  override def toString: String
}

object ViewRef {
  def apply(viewKey: ViewKey): ViewRef = ViewRefUsingViewKey(viewKey)

  def apply(viewResourceIdName: String, rIdClasses: Seq[Class[_]], innerClassName: String = "string"): ViewRef =
    ViewRefUsingName(viewResourceIdName, rIdClasses, innerClassName)

  def apply(viewResourceIdName: String, clazz: Class[_], innerClassName: String): ViewRef =
    apply(viewResourceIdName, detectRIdClasses(clazz), innerClassName)
}

private case class ViewRefUsingViewKey(viewKey: ViewKey) extends ViewRef {
  val viewKeyOpt = Some(viewKey)

  def viewKeyOrError = viewKey

  def fieldName(rIdClasses: Seq[Class[_]]): String = {
    resourceFieldWithIntValue(rIdClasses, viewKey).getName
  }

  override def toString = viewKey.toString
}

private case class ViewRefUsingName(viewResourceIdName: String, rIdClasses: Seq[Class[_]], innerClassName: String = "string") extends ViewRef {
  lazy val viewKeyOpt = findResourceIdWithName(rIdClasses, viewResourceIdName)

  lazy val viewKeyOrError = resourceIdWithName(rIdClasses, viewResourceIdName, innerClassName)

  def fieldName(rIdClasses: Seq[Class[_]]): String = viewResourceIdName

  override def toString = viewResourceIdName
}
