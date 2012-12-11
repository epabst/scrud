package com.github.scrud

import com.github.triangle.GetterInput

/**
 * The main GetterInput items that are used when copying data.
 * Normally, the data for the current entity is prepended to the GetterInput with its +: method before actually using.
 * This is better than a simple GetterInput because it provides access to prominent values.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/7/12
 * Time: 11:32 PM
 */
class CrudContextItems(val currentUriPath: UriPath, val crudContext: CrudContext, items: AnyRef*)
    extends GetterInput(currentUriPath +: crudContext +: items)
