package com.github.scrud.platform.representation

/**
 * A TargetType for the UI for identifying an entity instance such as when selecting or showing a reference to it.
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
@deprecated("use DisplayUI(FieldLevel.Identity)", since = "2014-08-04")
object SelectUI extends DisplayUI(FieldLevel.Identity)
