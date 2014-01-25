package com.github.scrud.util

import java.io.{FileWriter, File}

/**
 * An enhanced File.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 6:02 PM
 */
case class RichFile(file: File) {
  def /(name: String): File = {
    new File(file, name)
  }

  def writeAll(strings: String*) {
    val writer = new FileWriter(file)
    strings.foreach(writer.write(_))
    writer.close()
  }
}

/**
 * Utilities for working with files based on scala.tools.nsc.io.File and Path.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/7/12
 *         Time: 3:28 PM
 */

object FileConversions {
  implicit def toRichFile(file: File): RichFile = RichFile(file)
}

object Path {
  def apply(path: String): File = new File(path)
}
