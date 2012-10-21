package com.github.scrud.android

import com.github.scrud.persistence._
import com.github.triangle._
import com.github.scrud.{ParentField, CrudApplication, EntityType}

/** An entity configuration that provides all custom information needed to
  * implement CRUD on the entity.  This shouldn't depend on the platform (e.g. android).
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudType(val entityType: EntityType, val persistenceFactory: PersistenceFactory) extends Logging { self =>
  protected def logTag = entityType.logTag

  trace("Instantiated CrudType: " + this)

  def entityName = entityType.entityName

  /** This uses isDeletable because it assumes that if it can be deleted, it can be added as well.
    * @see [[com.github.scrud.android.CrudType.isDeletable]].
    */
  lazy val isAddable: Boolean = persistenceFactory.canCreate

  /** @see [[com.github.scrud.persistence.PersistenceFactory.canDelete]]. */
  final lazy val isDeletable: Boolean = persistenceFactory.canDelete

  /** @see [[com.github.scrud.persistence.PersistenceFactory.canSave]]. */
  final lazy val isUpdateable: Boolean = persistenceFactory.canSave

  lazy val parentFields: Seq[ParentField] = entityType.deepCollect {
    case parentField: ParentField => parentField
  }

  def parentEntityTypes(application: CrudApplication): Seq[EntityType] = entityType.parentEntityNames.map(application.entityType(_))

  def childEntityTypes(application: CrudApplication): Seq[EntityType] = childEntities(application).map(_.entityType)

  /** The list of entities that refer to this one.
    * Those entities should have a ParentField (or foreignKey) in their fields list.
    */
  def childEntities(application: CrudApplication): Seq[CrudType] = {
    trace("childEntities: allCrudTypes=" + application.allEntityTypes + " self=" + self)
    application.allCrudTypes.filter { entity =>
      val parentEntityTypes = entity.parentEntityTypes(application)
      trace("childEntities: parents of " + entity.entityType + " are " + parentEntityTypes)
      parentEntityTypes.contains(self.entityType)
    }
  }

  def openEntityPersistence(crudContext: CrudContext): CrudPersistence =
    persistenceFactory.createEntityPersistence(entityType, crudContext)

  final def withEntityPersistence[T](crudContext: CrudContext)(f: CrudPersistence => T): T = {
    val persistence = openEntityPersistence(crudContext)
    try f(persistence)
    finally persistence.close()
  }
}
