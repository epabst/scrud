package com.github.scrud.types

/**
 * A qualified type that may go beyond its JVM class to include semantic meaning such as units,
 * usages, etc.  Some examples are a "phone number", "zip code", "name", "miles", "meters", date (without time),
 * time (without date), or timestamp.
 * A hierarchy of these is expected.
 * It can have immutable state such as an enum class or a reference to conversion utilities.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/21/13
 * Time: 7:45 AM
 */
abstract class QualifiedType[T](implicit val manifest: Manifest[T])
