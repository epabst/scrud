package com.github.scrud.android.backup

/**
 * A place to write backup data to.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/24/13
 *         Time: 3:11 PM
 */
trait BackupTarget {
  def writeEntity(key: String, map: Option[Map[String,Any]])
}
