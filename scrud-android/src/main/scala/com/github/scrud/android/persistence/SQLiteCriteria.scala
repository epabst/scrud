package com.github.scrud.android.persistence

/**
 * Information for querying for a Cursor.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/16/13
 *         Time: 7:57 AM
 */
case class SQLiteCriteria(selection: List[String] = Nil, selectionArgs: List[String] = Nil,
                          groupBy: Option[String] = None, having: Option[String] = None, orderBy: Option[String] = None)

