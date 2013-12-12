package com.github.scrud.copy

/**
 * Both a [[com.github.scrud.copy.TargetType]] and a [[com.github.scrud.copy.SourceType]]
 * in that copy can be copied to and from it.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/10/13
 * Time: 3:14 PM
 */
trait StorageType extends TargetType with SourceType
