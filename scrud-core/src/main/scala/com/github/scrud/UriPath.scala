package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.platform.IdFormat

/** A convenience wrapper for UriPath.
  * It helps in that UriPath.EMPTY is null when running unit tests, and helps prepare for multi-platform support.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class UriPath(segments: String*) {
  def /(segment: String): UriPath = UriPath(segments :+ segment:_*)

  def /(entityName: EntityName): UriPath = this / entityName.name

  def /(id: ID): UriPath = this / id.toString

  override lazy val toString = segments.mkString("/", "/", "")
}

object UriPath {
  private lazy val idFormat = IdFormat

  val EMPTY: UriPath = UriPath()

  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)

  def apply(string: String): UriPath = UriPath(toOption(string.stripPrefix("/")).map(_.split("/").toSeq).getOrElse(Nil):_*)

  def apply(entityName: EntityName): UriPath = UriPath(entityName.name)

  def apply(entityName: EntityName, id: ID): UriPath = UriPath(entityName.name, id.toString)

  private[UriPath] def replacePathSegments(uri: UriPath, f: Seq[String] => Seq[String]): UriPath = {
    val path = f(uri.segments)
    UriPath(path: _*)
  }

  def specify(uri: UriPath, finalSegments: String*): UriPath =
    replacePathSegments(uri, _.takeWhile(_ != finalSegments.head) ++ finalSegments.toList)

  def specify(uri: UriPath, entityName: EntityName): UriPath = UriPath.specify(uri, entityName.name)

  def specify(uri: UriPath, entityName: EntityName, id: ID): UriPath =
    specify(uri, entityName.name, id.toString)

  def specifyLastEntityName(uri: UriPath, entityName: EntityName): UriPath =
    UriPath.specify(uri, entityName.name +: findId(uri, entityName).map(_.toString).toList:_*)

  def findId(uri: UriPath, entityName: EntityName): Option[ID] =
    uri.segments.dropWhile(_ != entityName.name).toList match {
      case _ :: idString :: tail => idFormat.toValue(idString).toOption
      case _ => None
    }

  def lastEntityNameOption(uri: UriPath): Option[EntityName] =
    uri.segments.reverse.find(idFormat.toValue(_).isFailure).map(EntityName(_))

  def lastEntityNameOrFail(uri: UriPath): EntityName = lastEntityNameOption(uri).getOrElse {
    throw new IllegalArgumentException("an EntityName must be specified in the URI but uri=" + uri)
  }
}
