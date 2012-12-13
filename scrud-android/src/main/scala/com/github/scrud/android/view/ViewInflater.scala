package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes
import android.view.{ViewGroup, View, LayoutInflater}

/**
 * A wrapper for [[android.view.LayoutInflater]] that also knows the ViewKey to inflate.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/11/12
 * Time: 3:35 PM
 */
private[android] class ViewInflater(val viewKey: PlatformTypes.ViewKey, val layoutInflater: LayoutInflater) {
  def inflate(viewGroup: ViewGroup): View = {
    layoutInflater.inflate(viewKey, viewGroup, false)
  }
}
