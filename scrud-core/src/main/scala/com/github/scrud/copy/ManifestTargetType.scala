package com.github.scrud.copy

/**
 * A TargetType that is generated based on a Manifest.
 * This can be used to avoid having to pass in a TargetType explicitly where one could be inferred.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:16 PM
 */
case class ManifestTargetType[T <: AnyRef](implicit val targetManifest: Manifest[T]) extends TargetType
