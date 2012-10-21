package com.github.scrud

import com.github.triangle.{PortableField, Field}

/**
 * A PortableField for getting the UriPath.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:17 PM
 */
object UriField extends Field(PortableField.identityField[UriPath])
