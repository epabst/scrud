package com.github.scrud.model

import com.github.scrud.platform.PlatformTypes._

/** A trait with a primary key
  * @author Eric Pabst (epabst@gmail.com)
  */
trait IdPk {
  def id: Option[ID]

  def withId(id: Option[ID]): IdPk
}

trait MutableIdPk extends IdPk {
  var id: Option[ID] = None

  def withId(newId: Option[ID]) = {
    id = newId
    this
  }
}