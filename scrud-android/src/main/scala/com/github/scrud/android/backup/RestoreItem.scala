package com.github.scrud.android.backup

/**
 * An item to be restored from backup.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/23/13
 *         Time: 7:43 AM
 */
case class RestoreItem(key: String, map: Map[String,Any])
