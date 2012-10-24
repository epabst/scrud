package com.github.scrud.action

import com.github.scrud.EntityType

/**
 * An Create, Read, Update, or Delete operation on an EntityType.
 * This is related to [[com.github.scrud.android.action.AndroidOperation]] but this one does not assume the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/3/12
 * Time: 6:42 PM
 */
case class CrudOperation(entityType: EntityType, operationType: CrudOperationType.Value)

object CrudOperationType extends Enumeration {
  val Create = Value("Create")
  val List = Value("List")
  val Read = Value("Read")
  val Update = Value("Update")
  val Delete = Value("Delete")
}
