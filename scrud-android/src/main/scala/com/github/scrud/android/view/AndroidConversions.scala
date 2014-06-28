package com.github.scrud.android.view

import android.view.View
import android.view.View.OnClickListener
import android.net.Uri
import com.github.scrud.UriPath
import android.content.Context
import com.github.scrud.context.SharedContext
import com.github.scrud.android.CrudAndroidApplicationLike

/** Useful conversions for Android development. */
object AndroidConversions {
  import scala.collection.JavaConversions._
  implicit def toUriPath(uri: Uri): UriPath = UriPath(uri.getPathSegments.toList:_*)

  def authorityFor(applicationPackageName: String): String = applicationPackageName + ".provider"

  def authorityFor(application: CrudAndroidApplicationLike): String = authorityFor(application.getClass)

  def authorityFor(applicationClass: Class[_ <: CrudAndroidApplicationLike]): String = authorityFor(applicationClass.getPackage.getName)

  def baseUriFor(application: CrudAndroidApplicationLike): Uri = baseUriFor(application.getClass)

  def baseUriFor(applicationClass: Class[_ <: CrudAndroidApplicationLike]): Uri = baseUriFor(applicationClass.getPackage.getName)

  def baseUriFor(packageName: String): Uri = (new Uri.Builder).scheme("content").authority(authorityFor(packageName)).build()

  def toUri(uriPath: UriPath, context: Context): Uri = toUri(uriPath, context.getApplicationInfo.packageName)

  def toUri(uriPath: UriPath, sharedContext: SharedContext): Uri = toUri(uriPath, sharedContext.asInstanceOf[CrudAndroidApplicationLike].getClass)

  def toUri(uriPath: UriPath, applicationClass: Class[_ <: CrudAndroidApplicationLike]): Uri = toUri(uriPath, applicationClass.getPackage.getName)

  def toUri(uriPath: UriPath, packageName: String): Uri = withAppendedPath(baseUriFor(packageName), uriPath)

  def withAppendedPath(baseUri: Uri, relativePath: UriPath): Uri =
    relativePath.segments.foldLeft(baseUri)((uri, segment) => Uri.withAppendedPath(uri, segment))

  implicit def toOnClickListener(body: View => Unit) = new OnClickListener {
    def onClick(view: View) { body(view) }
  }
}
