package com.github.scrud.persistence

import com.github.scrud.UriPath

/**
 * A listener for data changing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 4:59 PM
 */
trait DataListener {
  def onChanged(uri: UriPath)
}
