package com.github.scrud.copy

/**
 * A convenience class for holding a reusable source and SourceType pair.
 * It should only be used when sparingly to avoid allocating too many objects for simple operations.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 9:27 AM
 */
case class SourceWithType(source: AnyRef, sourceType: SourceType)
