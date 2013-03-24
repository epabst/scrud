package com.github.scrud.android.view

import android.view.View
import android.view.View.OnClickListener
import android.net.Uri
import com.github.scrud.{CrudApplication, UriPath}
import android.content.Context

/** Useful conversions for Android development. */
object AndroidConversions {
  import scala.collection.JavaConversions._
  implicit def toUriPath(uri: Uri): UriPath = UriPath(uri.getPathSegments.toList:_*)

  def baseUriFor(application: CrudApplication): Uri = baseUriFor(application.packageName)

  def baseUriFor(authority: String): Uri = (new Uri.Builder).scheme("content").authority(authority).build()

  def toUri(uriPath: UriPath, context: Context): Uri = toUri(uriPath, context.getApplicationInfo.packageName)

  def toUri(uriPath: UriPath, application: CrudApplication): Uri = toUri(uriPath, application.packageName)

  def toUri(uriPath: UriPath, authority: String): Uri = withAppendedPath(baseUriFor(authority), uriPath)

  def withAppendedPath(baseUri: Uri, relativePath: UriPath): Uri =
    relativePath.segments.foldLeft(baseUri)((uri, segment) => Uri.withAppendedPath(uri, segment))

  implicit def toOnClickListener(body: View => Unit) = new OnClickListener {
    def onClick(view: View) { body(view) }
  }
}
