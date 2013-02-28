package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle._
import scala.Some

/**
 * A PortableField that has an ID for a (presumably different) EntityType.
 * @param field the entire PortableField for getting the ID for the entity (other than those added by EntityField).
 *              If it is incomplete, some lookups won't happen because the ID must be gettable for a lookup to work.
 *              The default is an emptyField.
 * @author Eric Pabst (epabst@gmail.com)
 */
case class EntityField[E <: EntityType](entityName: EntityName, field: PortableField[ID] = PortableField.emptyField)
    extends DelegatingPortableField[ID] {
  val fieldName = entityName.name.toLowerCase + "_id"

  protected val delegate = field + entityName.UriPathId

  override val toString = "EntityField(" + entityName + ", " + field + ")"

  /**
   * A PortableField that gets the value from a field on a different Entity.
   * It uses Persistence to find the Entity using its ID (as known from this EntityField),
   * then uses the fieldFromEntityType parameter to get a specific field on that Entity
   * and return its value, if available.
   */
  def indirectField[T](fieldFromEntityType: E => PortableField[T]): PortableField[T] = {
    Getter[T] {
      case getterInput @ CrudContextField(Some(crudContext)) && UriField(Some(uri)) if unapply(getterInput).map(_.isDefined).getOrElse(false) =>
        val entityId: ID = unapply(getterInput).get.get
        crudContext.withEntityPersistence(entityName) { persistence =>
          val field = fieldFromEntityType(persistence.entityType.asInstanceOf[E])
          persistence.find(entityId, uri).flatMap(field.getValue(_))
        }
    }
  }
}

object EntityField {
  def entityFields(field: BaseField): Seq[EntityField[_]] = field.deepCollect {
    case entityField: EntityField[_] => entityField
  }
}
