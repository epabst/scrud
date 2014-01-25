package com.github.scrud.persistence

/**
 * A [[com.github.scrud.persistence.PersistenceFactory]] for testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 10:23 PM
 */
object ListBufferPersistenceFactoryForTesting extends ListBufferPersistenceFactory[Map[String,Option[Any]]](Map.empty)
