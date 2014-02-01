package com.github.scrud.types

import com.github.scrud.platform.PlatformTypes.ID
import com.github.scrud.EntityName
import scala.util.Try
import java.nio.ByteBuffer
import org.apache.commons.codec.binary.Base64

/**
 * A [[com.github.scrud.types.QualifiedType]] for a set of ID's of a certain entity type.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/31/14
 *         Time: 3:16 PM
 */
case class MultiSelectQT(entityName: EntityName) extends StringConvertibleQT[Set[ID]] {
  import MultiSelectQT._

  def convertIdToString(value: ID): String = Base64.encodeBase64URLSafeString(toByteBuffer(value).array())

  def convertIdFromString(string: String): Try[ID] = {
    Try {
      val bytes: Array[Byte] = Base64.decodeBase64(string)
      val byteBuffer: ByteBuffer = ByteBuffer.allocate(bytes.length).put(bytes)
      toLong(byteBuffer.array())
    }
  }

  def convertToString(value: Set[ID]) = value.toSeq.sorted.map(convertIdToString(_)).mkString(delimiter, delimiter, delimiter)

  def convertFromString(string: String) = Try(string.split(delimiter).filterNot(_.isEmpty).map(convertIdFromString(_).get).toSet)

  private def toLong(bytes: Array[Byte], longSoFar: Long = 0, index: Int = 0): Long = {
    if (index < bytes.length) {
      toLong(bytes, (longSoFar << java.lang.Byte.SIZE) + bytes(index), index + 1)
    } else {
      longSoFar
    }
  }

  private def toByteBuffer(long: Long, additionalBytesToAllocate: Int = 0): ByteBuffer = {
    val buffer = if (long > Byte.MaxValue) {
      toByteBuffer(long >> java.lang.Byte.SIZE, additionalBytesToAllocate + 1)
    } else {
      ByteBuffer.allocate(1 + additionalBytesToAllocate)
    }
    buffer.put((long & Byte.MaxValue).asInstanceOf[Byte])
  }
}

object MultiSelectQT {
  val delimiter = ":"
  private[MultiSelectQT] val byteCountForLong = java.lang.Long.SIZE / java.lang.Byte.SIZE
}