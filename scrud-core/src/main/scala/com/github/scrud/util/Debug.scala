package com.github.scrud.util

/**
 * Debug Flags for different aspects of the system.
 * This is mainly because in Android, logging uses the application name as the tag.
 * These are vars to allow setting it at runtime (such as in a debugger).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/18/14
 */
object Debug {
  var threading: Boolean = false
}
