package com.github.scrud.action

import com.github.scrud.EntityName
import com.github.scrud.action.CrudOperationType.CrudOperationType

/**
 * An Create, Read, Update, or Delete operation on an EntityType.
 * This is related to [[com.github.scrud.action.Command]] but this one assumes it is a CRUD command.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/3/12
 * Time: 6:42 PM
 */
case class CrudOperation(entityName: EntityName, operationType: CrudOperationType)

/**
 * The kinds of operations that a user may want to do to one or more entities.
 * They intentionally mirror the HTTP Methods of POST, GET (on a collection or by id), PUT, and DELETE,
 * and are intended to be more user-friendly terms.
 * They explicitly support the concept of interactively creating or updating an entity.
 */
@deprecated("use CommandKey.*", since = "2014-03-29")
object CrudOperationType extends Enumeration {
  type CrudOperationType = Value
  val Create, List, Read, Update, Delete = Value
}
