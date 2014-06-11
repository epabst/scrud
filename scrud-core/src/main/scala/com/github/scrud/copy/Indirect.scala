package com.github.scrud.copy

import com.github.scrud.{EntityName, EntityType, FieldDeclaration}
import com.github.scrud.platform.PlatformTypes.ID

/**
 * A Representation that gets the value from a field on an entity using a foreign key field.
 * @param otherEntityIdField the foreign key field on the current entity
 * @param indirectFieldGetter a function to get the field on the other entity
 * @tparam E the EntityType of the other entity
 * @tparam V the type of the field value
 */
case class Indirect[E <: EntityType,V](otherEntityIdField: FieldDeclaration[ID],
                                       indirectFieldGetter: E => FieldDeclaration[V])
    extends ExtensibleAdaptableField[V] with Representation[V] {
  val otherEntityName: EntityName = otherEntityIdField.qualifiedType.asInstanceOf[EntityName]

  override def findSourceField(sourceType: SourceType): Option[SourceField[V]] = {
    for {
      otherEntityIdField <- otherEntityIdField.toAdaptableField.findSourceField(sourceType)
    } yield new IndirectSourceField[E,V](otherEntityName, otherEntityIdField,
      otherEntityType => indirectFieldGetter(otherEntityType).toAdaptableField.findSourceField(sourceType))
  }

  override def findTargetField(targetType: TargetType) = None
}

private class IndirectSourceField[E <: EntityType,V](otherEntityName: EntityName, otherEntityIdField: SourceField[ID],
                                                     indirectFieldOptGetter: E => Option[SourceField[V]]) extends SourceField[V] {
  /** Get some value or None from the given source. */
  override def findValue(source: AnyRef, context: CopyContext): Option[V] = {
    for {
      otherEntityId <- otherEntityIdField.findValue(source, context)
      uri = otherEntityName.toUri(otherEntityId)
      otherEntity <- context.persistenceConnection.persistenceFor(otherEntityName).find(uri)
      otherEntityType = context.entityTypeMap.entityType(otherEntityName).asInstanceOf[E]
      indirectField <- indirectFieldOptGetter(otherEntityType)
      result <- indirectField.findValue(otherEntity, context)
    } yield result
  }
}
