package com.github.scrud.context

import com.github.scrud.{UriPath, EntityType, EntityName}
import com.github.scrud.state.{ApplicationConcurrentMapVal, StateHolder, State}
import com.github.scrud.util.{Debug, UrgentFutureExecutor, DelegateLogging, ListenerHolder}
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap, DataListener}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.copy.{SourceType, TargetType, AdaptedValueSeq}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits
import Implicits.global

/**
 * The context that is shared among all [[com.github.scrud.context.CommandContext]]s
 * for a single application (as identified by an [[com.github.scrud.context.ApplicationName]]).
 * Some examples are:<ul>
 *   <li>A Servlet Context</li>
 *   <li>A running Android Application</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait SharedContext extends StateHolder with DelegateLogging {
  private lazy val executor = new UrgentFutureExecutor()

  private[scrud] object FuturePortableValueCache
    extends ApplicationConcurrentMapVal[(EntityType, UriPath, CommandContext),Future[Option[AdaptedValueSeq]]]

  def entityTypeMap: EntityTypeMap

  def applicationName: ApplicationName = entityTypeMap.applicationName

  def platformDriver: PlatformDriver = entityTypeMap.platformDriver

  val applicationState: State = new State

  @deprecated("use makeTemporaryStubCommandContext()", since = "2014-06-07")
  lazy val asStubCommandContext: StubCommandContext = makeTemporaryStubCommandContext()

  def makeTemporaryStubCommandContext(): StubCommandContext = new StubCommandContext(this)

  protected def loggingDelegate = applicationName

  def dataListenerHolder(entityName: EntityName): ListenerHolder[DataListener] =
    dataListenerHolder(entityTypeMap.entityType(entityName))

  def dataListenerHolder(entityType: EntityType): ListenerHolder[DataListener] =
    entityTypeMap.persistenceFactory(entityType).listenerHolder(entityType, this)

  def withPersistence[T](f: PersistenceConnection => T): T = {
    val persistenceConnection = openPersistence()
    try f(persistenceConnection)
    finally persistenceConnection.close()
  }

  @deprecated("use CommandContext.persistenceConnection", since = "2014-06-07")
  def openPersistence(): PersistenceConnection = makeTemporaryStubCommandContext().persistenceConnection

  def future[T](body: => T): Future[T] = {
    if (Debug.threading) debug("Scheduling future (in SharedContext)")
    val future = Future {
      if (Debug.threading) debug("Running future (in SharedContext)")
      val result = try {
        body
      } finally {
        if (Debug.threading) debug("Done running future (in SharedContext)")
      }
      result
    }
    if (Debug.threading) debug("Done scheduling future (in SharedContext)")
    future
  }

  private def cachedFuturePortableValueOptOrCalculate(entityType: EntityType, uriPathWithId: UriPath, commandContext: CommandContext)(calculate: => Option[AdaptedValueSeq]): Future[Option[AdaptedValueSeq]] = {
    val cache = FuturePortableValueCache.get(commandContext.sharedContext)
    val key = (entityType, uriPathWithId, commandContext)
    cache.get(key).getOrElse {
      val futurePortableValueOpt = executor.urgentFuture {
        commandContext.withExceptionReportingHavingDefaultReturnValue[Option[AdaptedValueSeq]](None) {
          calculate
        }
      }
      cache.putIfAbsent(key, futurePortableValueOpt).getOrElse(futurePortableValueOpt)
    }
  }
  def futurePortableValueOpt(entityType: EntityType, sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): Future[Option[AdaptedValueSeq]] = {
    cachedFuturePortableValueOptOrCalculate(entityType, sourceUriWithId, commandContext) {
      calculatePortableValueOpt(entityType, sourceUriWithId, targetType, commandContext)
    }
  }

  def futurePortableValueOpt(entityType: EntityType, sourceType: SourceType, source: AnyRef,
                             sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): Future[Option[AdaptedValueSeq]] = {
    cachedFuturePortableValueOptOrCalculate(entityType, sourceUriWithId, commandContext) {
      Some(calculatePortableValue(entityType, sourceType, source, sourceUriWithId, targetType, commandContext))
    }
  }

  protected def calculatePortableValueOpt(entityType: EntityType, sourceUriWithId: UriPath,
                                          targetType: TargetType, commandContext: CommandContext): Option[AdaptedValueSeq] = {
    val persistence = commandContext.persistenceFor(entityType.entityName)
    val sourceOpt = persistence.find(sourceUriWithId)
    sourceOpt.map { source =>
      calculatePortableValue(entityType, persistence.sourceType, source, sourceUriWithId, targetType, commandContext)
    }
  }

  protected def calculatePortableValue(entityType: EntityType, sourceType: SourceType, source: AnyRef,
                                       sourceUriWithId: UriPath, targetType: TargetType, commandContext: CommandContext): AdaptedValueSeq = {
    debug("Copying entityType=" + entityType + " from sourceUri=" + sourceUriWithId +
      " from sourceType=" + sourceType + " to targetType=" + targetType)
    entityType.copy(sourceType, source, sourceUriWithId, targetType, commandContext)
  }
}
