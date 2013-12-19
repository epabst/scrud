package com.github.scrud.copy

/**
 * Both a [[com.github.scrud.copy.Target]] and a [[com.github.scrud.copy.Source]]
 * in that copy can be copied to and from it.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/10/13
 * Time: 3:14 PM
 */
class Storage[D <: AnyRef](data: D)(implicit override val dataManifest: Manifest[D]) extends SimpleSource[D](data) with Target[D]
