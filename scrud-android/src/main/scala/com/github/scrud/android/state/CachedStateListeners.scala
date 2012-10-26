package com.github.scrud.android.state

import collection.mutable
import java.util.concurrent.CopyOnWriteArraySet
import scala.collection.JavaConversions._

/** Listeners that represent state and will listen to a various events. */
object CachedStateListeners extends LazyActivityVal[mutable.Set[CachedStateListener]](new CopyOnWriteArraySet[CachedStateListener]())
