package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, TargetType}

/**
 * A TargetType for the UI that shows only summary information (as opposed to full detail).
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
object SummaryUI extends TargetType with RepresentationByType
