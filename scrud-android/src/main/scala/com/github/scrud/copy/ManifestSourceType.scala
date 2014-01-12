package com.github.scrud.copy

/**
 * A SourceType that is generated based on a Manifest.
 * This can be used to avoid having to pass in a SourceType explicitly where one could be inferred.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:16 PM
 */
case class ManifestSourceType[S <: AnyRef](implicit val sourceManifest: Manifest[S]) extends SourceType
