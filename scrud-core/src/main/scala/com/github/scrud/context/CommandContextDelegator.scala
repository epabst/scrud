package com.github.scrud.context

import com.github.scrud.EntityNavigation
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.action.Undoable
import com.github.scrud.platform.PlatformTypes.SKey

/**
 * A CommandContextHolder that delegates.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/8/14
 *         Time: 11:25 PM*/
private[scrud] trait CommandContextDelegator extends CommandContextHolder {
  override def sharedContext: SharedContext = commandContext.sharedContext

  def entityNavigation: EntityNavigation = commandContext.entityNavigation

  def persistenceConnection: PersistenceConnection = commandContext.persistenceConnection

  /** The ISO 2 country such as "US". */
  def isoCountry: String = commandContext.isoCountry

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) {
    commandContext.allowUndo(undoable)
  }

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  override def displayMessageToUser(message: String) {
    commandContext.displayMessageToUser(message)
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  override def displayMessageToUserBriefly(messageKey: SKey) {
    commandContext.displayMessageToUserBriefly(messageKey)
  }
}
