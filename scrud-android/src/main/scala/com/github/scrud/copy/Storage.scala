package com.github.scrud.copy

/**
 * Both a [[com.github.scrud.copy.Target]] and a [[com.github.scrud.copy.Source]]
 * in that copy can be copied to and from it.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/10/13
 * Time: 3:14 PM
 */
abstract class Storage(storageType: StorageType) extends Source(storageType) with Target {
  def targetType = storageType
}
