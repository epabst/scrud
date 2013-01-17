package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.triangle._
import scala.Some

/**
 * A PortableField that has an ID for a (presumably different) EntityType.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait EntityField[E <: EntityType] extends PortableField[ID] {
  def entityName: EntityName

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
  def apply[E <: EntityType](entityName: EntityName): NamedEntityField[E] = new NamedEntityField[E](entityName)

  def entityFields(field: BaseField): Seq[NamedEntityField[_]] = field.deepCollect {
    case entityField: NamedEntityField[_] => entityField
  }

  def apply[E <: EntityType](entityName: EntityName, field: PortableField[ID]): EntityField[E] = {
    val _entityName = entityName
    new EntityField[E] with DelegatingPortableField[ID] {
      def entityName = _entityName

      protected def delegate = field
    }
  }
}

case class NamedEntityField[E <: EntityType](entityName: EntityName) extends DelegatingPortableField[ID] with EntityField[E] {
  val fieldName = entityName.name.toLowerCase + "_id"

  protected val delegate = entityName.UriPathId

  override val toString = "EntityField(" + entityName + ")"
}
