package com.github.scrud

import persistence.CrudPersistence
import platform.PlatformDriver
import platform.PlatformTypes._
import com.github.scrud.types.{IdQualifiedType, QualifiedType}
import com.github.scrud.util.{Logging, Common}
import com.github.scrud.copy._
import scala.collection.mutable
import com.github.scrud.platform.representation._
import com.github.scrud.context.CommandContext
import scala.util.Try
import com.github.scrud.model.{IdPk, IdPkField}
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.SortOrder.SortOrder

/**
 * A stateless configuration of an entity, providing information needed to map data to and from persistence, UI, model, etc.
 * Each subclass should call the field(String, QualifiedType[V], Seq[Representation[V]]) method for each field.
 * Ideally each subclass won't assume the platform (e.g. android) so that it can be re-used for multiple platforms.
 * @author Eric Pabst (epabst@gmail.com)
 * @param entityName  this is used to identify the EntityType and for internationalized strings
  */
abstract class EntityType(val entityName: EntityName, val platformDriver: PlatformDriver) extends AdaptableFieldSeq with Logging {
  override val logTag = Try(entityName.name).getOrElse(Common.logTag)

  trace("Instantiated EntityType: " + this)

  private val fieldDeclarationsBuffer: mutable.Buffer[BaseFieldDeclaration] = mutable.Buffer[BaseFieldDeclaration]()

  final lazy val fieldDeclarations: Seq[BaseFieldDeclaration] = fieldDeclarationsBuffer.toSeq

  lazy val currentPersistedFields: Seq[BaseFieldDeclaration] = persistedFields(Persistence.Latest.dataVersion)

  def persistedFields(dataVersion: Int): Seq[BaseFieldDeclaration] = for {
    fieldDeclaration <- fieldDeclarations
    adaptableField = fieldDeclaration.toAdaptableField
    if adaptableField.hasSourceFieldUsingSource(Persistence(dataVersion))
  } yield fieldDeclaration

  lazy val dataVersion: Int = {
    (for {
      fieldDeclaration <- fieldDeclarations
      persistenceRange <- fieldDeclaration.persistenceRangeOpt
    } yield {
      if (persistenceRange.maxDataVersion < Int.MaxValue) {
        persistenceRange.maxDataVersion + 1
      } else {
        persistenceRange.minDataVersion
      }
    }).max
  }

  /** The data version when this entity was originally created. */
  lazy val originalDataVersion: Int = {
    (for {
      fieldDeclaration <- fieldDeclarations
      persistenceRange <- fieldDeclaration.representations.collect {
        case persistenceRange: PersistenceRange => persistenceRange
      }
    } yield persistenceRange.minDataVersion).min
  }

  final lazy val adaptableFields: Seq[BaseAdaptableField] = fieldDeclarations.map(_.toAdaptableField)

  /**
   * Creates a new field for this entity.
   * This is the most important method in this class and may be the only method needed
   * when creating an Entity.
   * @param fieldName the name of the field.  It is assumed to be camel-case by convention.
   * @param qualifiedType the data type of the field.  These can be well-known types or custom sub-types, as long as the PlatformDriver(s) can handle it.
   * @param representations the various representations that the field can have.  This may include Persistence, UI, Model, etc.
   * @tparam V the Java data type for the field.
   * @return an AdaptableField which can be ignored since it is automatically stored in the EntityType.
   *         It does not return an ExtensibleAdaptableField since any extensions would not be registered.
   */
  protected def field[V](fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): FieldDeclaration[V] = {
    val newFieldDeclaration = FieldDeclaration(entityName, FieldName(fieldName), qualifiedType, representations ++ impliedRepresentations, platformDriver)
    fieldDeclarationsBuffer += newFieldDeclaration
    newFieldDeclaration
  }

  /** These Representations are included in ALL fields. Override this to add or remove some. */
  val impliedRepresentations: Seq[Representation[Nothing]] = Seq(MapStorage)

  /**
   * Creates a field that references another entity (by ID).
   * @param qualifiedType the EntityName of the entity to have a reference to.
   * @param representations the various representations that the field can have.  This may include Persistence, UI, Model, etc.
   * @tparam V the Java data type for the field.
   * @return an AdaptableField which can be ignored since it is automatically stored in the EntityType.
   */
  protected def field[V](qualifiedType: QualifiedTypeProvidingFieldName[V], representations: Seq[Representation[V]]): FieldDeclaration[V] =
    field[V](qualifiedType.toFieldName, qualifiedType, representations)

  /**
   * Specifies the name of the ID field.
   * Normally this should be dictated by the PlatformDriver since some platforms
   * (e.g. Android with SQLite) need the field to have a specific name.
   * @return a Seq of Representation
   * @see [[com.github.scrud.EntityType.idField]]
   */
  def idFieldName: String = platformDriver.idFieldName(entityName)

  /**
   * Specifies the Representations that an ID has for this entity.
   * @return a Seq of Representation
   * @see [[com.github.scrud.EntityType.idField]]
   */
  protected def idFieldRepresentations: Seq[Representation[ID]] =
    Seq(Persistence(Int.MinValue), Query, EntityModelForPlatform, MapStorage, IdPkField,
      Calculation { context => UriPath.findId(context.sourceUri, entityName) })

  /**
   * The ID field declaration for this entity.
   * This calls [[com.github.scrud.EntityType.field]] with a type of [[com.github.scrud.types.IdQualifiedType]].
   * Rather than overriding this, it is recommended to override
   * [[com.github.scrud.EntityType.idFieldName]] and/or [[com.github.scrud.EntityType.idFieldRepresentations]].
   */
  val id: FieldDeclaration[ID] = field(idFieldName, IdQualifiedType, idFieldRepresentations)

  /**
   * The ID field for this entity.
   * This calls [[com.github.scrud.EntityType.field]] with a type of [[com.github.scrud.types.IdQualifiedType]].
   * Rather than overriding this, it is recommended to override
   * [[com.github.scrud.EntityType.idFieldName]] and/or [[com.github.scrud.EntityType.idFieldRepresentations]].
   */
  def idField: AdaptableField[ID] = id.toAdaptableField

  def sortOrder: Seq[(BaseFieldDeclaration,SortOrder)] = Seq.empty

  lazy val entityReferenceFieldDeclarations: Seq[FieldDeclaration[ID]] = for {
    fieldDeclaration <- fieldDeclarations
    if fieldDeclaration.qualifiedType.isInstanceOf[EntityName]
    entityReferenceFieldDeclaration = fieldDeclaration.asInstanceOf[FieldDeclaration[ID]]
  } yield entityReferenceFieldDeclaration

  lazy val referencedEntityNames: Seq[EntityName] = entityReferenceFieldDeclarations.map(_.qualifiedType.asInstanceOf[EntityName])

  def clearId(source: IdPk): IdPk = source.withId(None)

  def clearId(source: AnyRef): AnyRef = new UnsupportedOperationException

  def copyAndUpdate[T <: AnyRef](sourceType: SourceType, source: AnyRef, sourceUri: UriPath, targetType: InstantiatingTargetType[T], commandContext: CommandContext): T =
    copyAndUpdate(sourceType, source, sourceUri, targetType, targetType.makeTarget(commandContext), commandContext)

  def copyAndUpdate[T <: AnyRef](sourceType: SourceType, source: AnyRef, sourceUri: UriPath,
                                 targetType: TargetType, target: T, commandContext: CommandContext): T = {
    val adaptedFieldSeq = adapt(sourceType, targetType)
    adaptedFieldSeq.copyAndUpdate(source, sourceUri, target, commandContext)
  }

  def copy(sourceType: SourceType, source: AnyRef, sourceUri: UriPath,
           targetType: TargetType, commandContext: CommandContext): AdaptedValueSeq = {
    val adaptedFieldSeq = adapt(sourceType, targetType)
    adaptedFieldSeq.copy(source, sourceUri, commandContext)
  }

  def toUri: UriPath = entityName.toUri

  def toUri(id: ID): UriPath = entityName.toUri(id)

  def toUri(idOpt: Option[ID]): UriPath = entityName.toUri(idOpt)

  /**
   * Available to be overridden as needed by applications.
   * This is especially useful to create any initial data.
   * @param lowLevelPersistence The CrudPersistence as provided by [[com.github.scrud.platform.PlatformDriver.localDatabasePersistenceFactory]].
   *                            It is not wrapped by any other CrudPersistence so it may be matched as needed for additional access depending on the platform and implementation.
   */
  def onCreateDatabase(lowLevelPersistence: CrudPersistence) {}

  override def toString = entityName.toString
}
