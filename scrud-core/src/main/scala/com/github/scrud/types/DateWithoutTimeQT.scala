package com.github.scrud.types

import java.util.Date

/**
 * A QualifiedType for a Date that does not include the time on the given day.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object DateWithoutTimeQT extends QualifiedType[Date]
