package com.github.scrud.android.persistence

import android.database.Cursor
import android.content.ContentValues
import android.os.Bundle
import com.github.scrud.types._
import scala.collection.concurrent
import com.github.scrud.EntityName

private[persistence] trait BasePersistedType {
  def sqliteType: String
}

/** A persisted type.  It should be very simple and serializable, ideally a primitive. */
trait PersistedType[T] extends BasePersistedType {
  def getValue(cursor: Cursor, cursorIndex: Int): Option[T]

  def getValue(contentValues: ContentValues, name: String): Option[T]

  def putValue(contentValues: ContentValues, name: String, value: T)

  def getValue(bundle: Bundle, name: String): Option[T]

  def putValue(bundle: Bundle, name: String, value: T)
}

/** An android PersistedType based on the [[android.database.Cursor]] and [[android.content.ContentValues]] api's.
  * @author Eric Pabst (epabst@gmail.com)
  */

private[persistence] class ConvertedPersistedType[T,P](toValue: P => Option[T], toPersisted: T => P)
                                                      (implicit persistedType: PersistedType[P])
        extends PersistedType[T] {
  val sqliteType = persistedType.sqliteType

  def getValue(contentValues: ContentValues, name: String): Option[T] =
    persistedType.getValue(contentValues, name).flatMap(toValue)

  def putValue(contentValues: ContentValues, name: String, value: T) {
    persistedType.putValue(contentValues, name, toPersisted(value))
  }

  def getValue(cursor: Cursor, cursorIndex: Int): Option[T] = persistedType.getValue(cursor, cursorIndex).flatMap(toValue)

  def putValue(bundle: Bundle, name: String, value: T) {
    persistedType.putValue(bundle, name, toPersisted(value))
  }

  def getValue(bundle: Bundle, name: String): Option[T] = persistedType.getValue(bundle, name).flatMap(toValue)
}

private class DirectPersistedType[T <: AnyRef](val sqliteType: String,
                                               cursorGetter: Cursor => Int => Option[T],
                                               contentValuesGetter: ContentValues => String => Option[T],
                                               contentValuesPutter: ContentValues => (String, T) => Unit,
                                               bundleGetter: Bundle => String => Option[T], bundlePutter: Bundle => (String, T) => Unit) extends PersistedType[T] {
  def putValue(contentValues: ContentValues, name: String, value: T) {
    contentValuesPutter(contentValues)(name, value)
  }

  def getValue(cursor: Cursor, cursorIndex: Int): Option[T] = if (cursor.isNull(cursorIndex)) None else cursorGetter(cursor)(cursorIndex)

  def getValue(contentValues: ContentValues, name: String): Option[T] = contentValuesGetter(contentValues)(name)

  def putValue(bundle: Bundle, name: String, value: T) {
    bundlePutter(bundle)(name, value)
  }

  def getValue(bundle: Bundle, name: String): Option[T] = if (bundle.containsKey(name)) bundleGetter(bundle)(name) else None
}

object PersistedType {
  private val persistedTypeByQualifiedType: concurrent.Map[BaseQualifiedType,BasePersistedType] =
    concurrent.TrieMap[BaseQualifiedType,BasePersistedType]()

  def apply(qualifiedType: BaseQualifiedType): BasePersistedType =
    apply(qualifiedType.asInstanceOf[QualifiedType[_]])

  def apply[T](qualifiedType: QualifiedType[T]): PersistedType[T] = {
    qualifiedType match {
      case q: LongQualifiedType => enforceTypeMatch[Long,T](q, PersistedType.longType)
      case q: EntityName => enforceTypeMatch[Long,T](q, PersistedType.longType)
      case q: IntQualifiedType => enforceTypeMatch[Int,T](q, PersistedType.intType)
      case q: StringQualifiedType => enforceTypeMatch[String,T](q, PersistedType.stringType)
      case q: DoubleQualifiedType => enforceTypeMatch[Double,T](q, PersistedType.doubleType)
      case q: FloatQualifiedType => enforceTypeMatch[Float,T](q, PersistedType.floatType)
      case q: StringConvertibleQT[_] => toConvertedPersistedType(q).asInstanceOf[PersistedType[T]]
    }
  }

  private def toConvertedPersistedType[T](stringConvertibleQT: StringConvertibleQT[T]): PersistedType[T] = {
    persistedTypeByQualifiedType.getOrElseUpdate(stringConvertibleQT, {
      new ConvertedPersistedType[T, String](
        stringConvertibleQT.convertFromString(_).toOption,
        stringConvertibleQT.convertToString(_))
    }).asInstanceOf[PersistedType[T]]
  }

  private def enforceTypeMatch[T,RT](qualifiedType: QualifiedType[T], persistedType: PersistedType[T]): PersistedType[RT] =
    persistedType.asInstanceOf[PersistedType[RT]]

  private class RichBundle(bundle: Bundle) {
    implicit def getJLong(key: String): java.lang.Long = bundle.getLong(key)
    implicit def putJLong(key: String, value: java.lang.Long) { bundle.putLong(key, value.longValue) }
    implicit def getJInt(key: String): java.lang.Integer = bundle.getInt(key)
    implicit def putJInt(key: String, value: java.lang.Integer) { bundle.putInt(key, value.intValue) }
    implicit def getJShort(key: String): java.lang.Short = bundle.getShort(key)
    implicit def putJShort(key: String, value: java.lang.Short) { bundle.putShort(key, value.shortValue) }
    implicit def getJByte(key: String): java.lang.Byte = bundle.getByte(key)
    implicit def putJByte(key: String, value: java.lang.Byte) { bundle.putByte(key, value.byteValue) }
    implicit def getJDouble(key: String): java.lang.Double = bundle.getDouble(key)
    implicit def putJDouble(key: String, value: java.lang.Double) { bundle.putDouble(key, value.doubleValue) }
    implicit def getJFloat(key: String): java.lang.Float = bundle.getFloat(key)
    implicit def putJFloat(key: String, value: java.lang.Float) { bundle.putFloat(key, value.floatValue) }
  }
  private implicit def toRichBundle(bundle: Bundle): RichBundle = new RichBundle(bundle)
  private class RichCursor(cursor: Cursor) {
    def getByte(index: Int): Byte = cursor.getShort(index).asInstanceOf[Byte]
    def getJLong(index: Int): java.lang.Long = cursor.getLong(index).asInstanceOf[java.lang.Long]
    def getJInt(index: Int): java.lang.Integer = cursor.getInt(index).asInstanceOf[java.lang.Integer]
    def getJShort(index: Int): java.lang.Short = cursor.getShort(index).asInstanceOf[java.lang.Short]
    def getJByte(index: Int): java.lang.Byte = cursor.getShort(index).asInstanceOf[java.lang.Byte]
    def getJDouble(index: Int): java.lang.Double = cursor.getDouble(index).asInstanceOf[java.lang.Double]
    def getJFloat(index: Int): java.lang.Float = cursor.getFloat(index).asInstanceOf[java.lang.Float]
  }
  private implicit def toRichCursor(cursor: Cursor): RichCursor = new RichCursor(cursor)
  implicit lazy val stringType: PersistedType[String] = directPersistedType[String]("TEXT", c => c.getString,
    c => f => Option(c.getAsString(f)), c => c.put(_, _), c => c.getString, c => c.putString(_, _))
  implicit lazy val longRefType: PersistedType[java.lang.Long] = directPersistedType[java.lang.Long]("INTEGER", c => c.getJLong,
    c => f => Option(c.getAsLong(f)), c => c.put(_, _), c => c.getJLong, c => c.putJLong)
  implicit lazy val intRefType: PersistedType[java.lang.Integer] = directPersistedType[java.lang.Integer]("INTEGER", c => c.getJInt,
    c => f => Option(c.getAsInteger(f)), c => c.put(_, _), c => c.getJInt, c => c.putJInt)
  implicit lazy val shortRefType: PersistedType[java.lang.Short] = directPersistedType[java.lang.Short]("INTEGER", c => c.getJShort,
    c => f => Option(c.getAsShort(f)), c => c.put(_, _), c => c.getJShort, c => c.putJShort)
  implicit lazy val byteRefType: PersistedType[java.lang.Byte] = directPersistedType[java.lang.Byte]("INTEGER", c => c.getJByte,
    c => f => Option(c.getAsByte(f)), c => c.put(_, _), c => c.getJByte, c => c.putJByte)
  implicit lazy val doubleRefType: PersistedType[java.lang.Double] = directPersistedType[java.lang.Double]("REAL", c => c.getJDouble,
    c => f => Option(c.getAsDouble(f)), c => c.put(_, _), c => c.getJDouble, c => c.putJDouble)
  implicit lazy val floatRefType: PersistedType[java.lang.Float] = directPersistedType[java.lang.Float]("REAL", c => c.getJFloat,
    c => f => Option(c.getAsFloat(f)), c => c.put(_, _), c => c.getJFloat, c => c.putJFloat)
  implicit lazy val blobType: PersistedType[Array[Byte]] = directPersistedType[Array[Byte]]("BLOB", c => c.getBlob,
    c => f => Option(c.getAsByteArray(f)), c => c.put(_, _), c => c.getByteArray, c => c.putByteArray(_, _))
  implicit lazy val longType: PersistedType[Long] = castedPersistedType[Long,java.lang.Long]
  implicit lazy val intType: PersistedType[Int] = castedPersistedType[Int,java.lang.Integer]
  implicit lazy val shortType: PersistedType[Short] = castedPersistedType[Short,java.lang.Short]
  implicit lazy val byteType: PersistedType[Byte] = castedPersistedType[Byte,java.lang.Byte]
  implicit lazy val doubleType: PersistedType[Double] = castedPersistedType[Double,java.lang.Double]
  implicit lazy val floatType: PersistedType[Float] = castedPersistedType[Float,java.lang.Float]

  /** P is the persisted type
    * T is the value type
    */
  def castedPersistedType[T,P](implicit persistedType: PersistedType[P]): PersistedType[T] =
    new ConvertedPersistedType[T,P](p => Option(p.asInstanceOf[T]), v => v.asInstanceOf[P])

  //doesn't require an Option.
  private def directPersistedType[T <: AnyRef](sqliteType: String,
                                               cursorGetter: Cursor => Int => T,
                                               contentValuesGetter: ContentValues => String => Option[T],
                                               contentValuesPutter: ContentValues => (String, T) => Unit,
                                               bundleGetter: Bundle => String => T, bundlePutter: Bundle => (String, T) => Unit) =
    new DirectPersistedType(sqliteType, c => index => Some(cursorGetter(c)(index)), contentValuesGetter,
      contentValuesPutter, b => k => Some(bundleGetter(b)(k)), bundlePutter)
}
