package com.github.scrud.android.persistence

import android.database.AbstractCursor
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.copy.SourceField

/**
 * A Cursor that wraps the result of a [[com.github.scrud.persistence.CrudPersistence.findAll]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/20/13
 * Time: 5:33 PM
 */
class CrudCursor(findAllResult: Seq[AnyRef], entityTypePersistedInfo: EntityTypePersistedInfo) extends AbstractCursor2 {
  //todo cache this in the applicationContext
  private lazy val persistedSourceFields: Seq[SourceField[Any]] =
    entityTypePersistedInfo.currentPersistedFields.map(_.toAdaptableField.sourceFieldOrFail(Persistence.Latest))

  lazy val getColumnNames = entityTypePersistedInfo.currentPersistedFieldNames.toArray

  private val unusedCopyContext = null

  def getCount = findAllResult.size

  private def getValue[T](columnIndex: Int, fromString: String => T): T =
    getValueOpt(columnIndex).getOrElse(fromString(null)) match {
      case s: String => fromString(s)
      case t: T => t
      case x => fromString(x.toString)
    }

  private def getValueOpt(columnIndex: Int): Option[Any] = {
    persistedSourceFields(columnIndex).findValue(findAllResult(getPosition), unusedCopyContext)
  }

  def getDouble(columnIndex: Int) = getValue(columnIndex, java.lang.Double.parseDouble(_))

  def getFloat(columnIndex: Int) = getValue(columnIndex, java.lang.Float.parseFloat(_))

  def getInt(columnIndex: Int) = getValue(columnIndex, java.lang.Integer.parseInt(_))

  def getLong(columnIndex: Int) = getValue(columnIndex, java.lang.Long.parseLong(_))

  def getShort(columnIndex: Int) = getValue(columnIndex, java.lang.Short.parseShort(_))

  def getString(columnIndex: Int) = getValueOpt(columnIndex).map(_.toString).getOrElse(null)

  def isNull(columnIndex: Int) = getValueOpt(columnIndex).isEmpty
}

/** This only exists because ShadowAbstractCursor in robolectric isn't using the getColumnNames method for these. */
abstract class AbstractCursor2 extends AbstractCursor {
  override def getColumnName(column: Int): String = getColumnNames.apply(column)

  override def getColumnIndex(columnName: String): Int = getColumnNames.indexOf(columnName)
}
