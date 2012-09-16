package com.github.scrud.android.state

import com.github.scrud.android.persistence.EntityType
import com.github.scrud.android.common.{Common, UriPath}
import com.github.scrud.android.CrudContext
import com.github.triangle.{PortableField, GetterInput, Logging, PortableValue}

object EntityValueCachedFunction extends CachedActivityFunction[(EntityType, UriPath, CrudContext), Option[PortableValue]] with Logging {
  protected def logTag = Common.logTag

  /** Specify the actual function to use when the result has not been cached for a given Activity. */
  protected def evaluate(input: (EntityType, UriPath, CrudContext)) = {
    val (entityType, uriPathWithId, crudContext) = input
    val contextItems = GetterInput(uriPathWithId, crudContext, PortableField.UseDefaults)
    crudContext.withEntityPersistence(entityType)(_.find(uriPathWithId).map {
      readable =>
        debug("Copying " + entityType.entityName + "#" + entityType.IdField.getRequired(readable))
        entityType.copyFrom(readable +: contextItems)
    })
  }
}
