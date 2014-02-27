package com.github.scrud.query

import com.github.scrud.EntityFieldPath

/**
 * Part of the criteria for an EntityQuery.
 * Examples:<ul>
 * <li>Book Query: Book.author.id=45 (from a given Author, query for Books)</li>
 * <li>Author Query: Author.id=45 (from a given Book, query for Author)</li>
 * <li>Book.author.publisher.id=77(</li>
 * </ul>
 */
case class FieldPathValue[V](fieldPath: EntityFieldPath[V], value: V) extends EntityCriterion
