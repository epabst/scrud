package com.github.scrud.view

/**
 * A Map of View name to values.
 * Wraps a map so that it is distinguished from persisted fields.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 1/4/13
 * Time: 5:51 AM
 */
case class NamedViewMap(map: Map[String,Option[Any]]) {
  def contains(name: String) = map.contains(name)
  def apply(name: String) = map.apply(name)
  def get(name: String) = map.get(name)
  def iterator = map.iterator
  def -(name: String) = NamedViewMap(map - name)
  def +[B1 >: Any](kv: (String, Option[B1])) = NamedViewMap(map + kv)
}

object NamedViewMap {
  val empty = NamedViewMap()
  def apply(elems: (String,Option[Any])*): NamedViewMap = new NamedViewMap(Map(elems: _*))
}
