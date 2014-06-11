package com.github.scrud.android.persistence

import android.net.Uri

/**
 * Information for querying for a content Cursor.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/16/13
 *         Time: 7:57 AM
 */
case class ContentQuery(uri: Uri, projection: Seq[String], selection: Seq[String] = Nil,
                        selectionArgs: Seq[String] = Nil, sortOrder: Option[String] = None)
