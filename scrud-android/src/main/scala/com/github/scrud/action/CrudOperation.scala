package com.github.scrud.action

import com.github.scrud.EntityName

/**
 * An Create, Read, Update, or Delete operation on an EntityType.
 * This is related to [[com.github.scrud.android.action.AndroidOperation]] but this one does not assume the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/3/12
 * Time: 6:42 PM
 */
case class CrudOperation(entityName: EntityName, operationType: CrudOperationType.Value)

/**
 * The kinds of operations that a user may want to do to one or more entities.
 * They intentionally mirror the HTTP Methods of POST, GET (on a collection or by id), PUT, and DELETE,
 * and are intended to be more user-friendly terms.
 * They explicitly support the concept of interactively creating or updating an entity.
 */
object CrudOperationType extends Enumeration {
  val Create = Value("Create")
  val List = Value("List")
  val Read = Value("Read")
  val Update = Value("Update")
  val Delete = Value("Delete")
}
