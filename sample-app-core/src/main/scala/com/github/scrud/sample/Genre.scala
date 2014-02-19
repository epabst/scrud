package com.github.scrud.sample

/** A Book Genre.
  * @author pabstec
  */

object Genre extends Enumeration {
  type Genre = Value
  val Fantasy = Value("Fantasy")
  val Romance = Value("Romance")
  val Child = Value("Child")
  val Nonfiction = Value("Non-Fiction")
  val SciFi = Value("Sci-Fi")
  val Other = Value("Other")
}
