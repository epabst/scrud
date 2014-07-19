package com.github.scrud.action

/**
 * An undo of an operation.  The operation should have already completed, but it can be undone or accepted.
 * @param undoAction  An Action that reverses the operation.
 * @param closeOperation  An operation that releases any resources, and is guaranteed to be called.
 *           For example, if undo was not called, it could delete related entities.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/29/12
 * Time: 7:37 AM
 */
case class Undoable(undoAction: OperationAction, closeOperation: Option[PersistenceOperation] = None) {
  /**
   * Creates a new Undoable that wraps this one and does an additional Operation when undoing.
   * It is patterned after [[scala.Function1.andThen]].
   */
  def andThen(nextOperation: Operation): Undoable = Undoable(undoAction.andThen(nextOperation), closeOperation)
}
