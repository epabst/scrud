package com.github.scrud.copy

/**
 * A StorageField that works for a specific type of source and target.
 */
abstract class TypedStorageField[D <: AnyRef,V] extends TypedTargetField[D,V] with TypedSourceField[D,V]
