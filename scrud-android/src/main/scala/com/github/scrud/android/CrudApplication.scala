package com.github.scrud.android

import action._
import common.{UriPath, Common}
import java.util.NoSuchElementException
import persistence.EntityType
import com.github.triangle.{GetterInput, PortableField, PortableValue, Logging}

/** An Application that works with [[com.github.scrud.android.CrudType]]s.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/31/11
 * Time: 4:50 PM
 */

trait CrudApplication extends Logging {
  def logTag = Common.tryToEvaluate(nameId).getOrElse(Common.logTag)

  trace("Instantiated CrudApplication: " + this)

  def name: String

  /** The version of the data such as a database.  This must be increased when new tables or columns need to be added, etc. */
  def dataVersion: Int

  //this will be used for programmatic uses such as a database name
  lazy val nameId: String = name.replace(" ", "_").toLowerCase

  def classNamePrefix: String = getClass.getSimpleName.replace("$", "").stripSuffix("Application")
  def packageName: String = getClass.getPackage.getName

  /** All entities in the application, in priority order of most interesting first. */
  def allCrudTypes: Seq[CrudType]
  def allEntityTypes: Seq[EntityType] = allCrudTypes.map(_.entityType)

  /** The EntityType for the first page of the App. */
  def primaryEntityType: EntityType = allEntityTypes.head

  lazy val contentProviderAuthority = packageName
  // The first EntityType is used as the default starting point.
  lazy val defaultContentUri = UriPath("content://" + contentProviderAuthority) / primaryEntityType.entityName

  def childEntityTypes(entityType: EntityType): Seq[EntityType] = crudType(entityType).childEntityTypes(this)

  final def withEntityPersistence[T](entityType: EntityType, activity: ActivityWithState)(f: CrudPersistence => T): T = {
    crudType(entityType).withEntityPersistence(new CrudContext(activity, this))(f)
  }

  def crudType(entityType: EntityType): CrudType =
    allCrudTypes.find(_.entityType == entityType).getOrElse(throw new NoSuchElementException(entityType + " not found"))

  private def persistenceFactory(entityType: EntityType): PersistenceFactory = crudType(entityType).persistenceFactory

  def newWritable(entityType: EntityType): AnyRef = persistenceFactory(entityType).newWritable

  /** Returns true if the URI is worth calling EntityPersistence.find to try to get an entity instance. */
  def maySpecifyEntityInstance(uri: UriPath, entityType: EntityType): Boolean =
    persistenceFactory(entityType).maySpecifyEntityInstance(entityType, uri)

  def isListable(entityType: EntityType): Boolean = persistenceFactory(entityType).canList

  def isSavable(entityType: EntityType): Boolean = persistenceFactory(entityType).canSave

  def isAddable(entityType: EntityType): Boolean = isDeletable(entityType)

  def isDeletable(entityType: EntityType): Boolean = persistenceFactory(entityType).canDelete

  def actionsForEntity(entityType: EntityType): Seq[Action] = crudType(entityType).getEntityActions(this)

  def actionsForList(entityType: EntityType): Seq[Action] = crudType(entityType).getListActions(this)

  def actionToCreate(entityType: EntityType): Option[Action] = crudType(entityType).createAction

  def actionToUpdate(entityType: EntityType): Option[Action] = crudType(entityType).updateAction

  def actionToDelete(entityType: EntityType): Option[Action] = crudType(entityType).deleteAction

  def actionToList(entityType: EntityType): Option[Action] = Some(crudType(entityType).listAction)

  def actionToDisplay(entityType: EntityType): Option[Action] = Some(crudType(entityType).displayAction)

  def copyFromPersistedEntity(entityType: EntityType, uriPathWithId: UriPath, crudContext: CrudContext): Option[PortableValue] = {
    val contextItems = GetterInput(uriPathWithId, crudContext, PortableField.UseDefaults)
    crudContext.withEntityPersistence(entityType)(_.find(uriPathWithId).map { readable =>
      debug("Copying " + entityType.entityName + "#" + entityType.IdField.getRequired(readable) + " to " + this)
      entityType.copyFromItem(readable +: contextItems)
    })
  }
}
