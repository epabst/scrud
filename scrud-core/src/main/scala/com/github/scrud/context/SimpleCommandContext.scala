package com.github.scrud.context

import com.github.scrud.action.{Undoable, CrudOperationType}
import com.github.scrud.{EntityNavigation, UriPath}
import com.github.scrud.state.SimpleStateHolder
import com.github.scrud.platform.PlatformTypes
import com.github.scrud.action.CrudOperationType.CrudOperationType

/**
 * A simple implementation of a CommandContext.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 2:10 PM
 */
case class SimpleCommandContext(operationType: CrudOperationType, uri: UriPath, sharedContext: SharedContext,
                                entityNavigation: EntityNavigation) extends CommandContext {
  override def withUri(uri: UriPath) = copy(uri = uri)

  val stateHolder = new SimpleStateHolder

  /** The ISO 2 country such as "US". */
  lazy val isoCountry = java.util.Locale.getDefault.getCountry

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String) {
    println("Message to User: " + message)
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    println("Message Key to User: " + messageKey)
  }

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) {
    println("Allowed Undo: " + undoable)
  }
}
