package com.github.scrud.android.state

import collection.mutable
import java.util.concurrent.CopyOnWriteArraySet
import com.github.scrud.state.LazyActivityVal
import scala.collection.JavaConversions._

/** Listeners that represent state and will listen to a various events. */
object CachedStateListeners extends LazyActivityVal[mutable.Set[CachedStateListener]](new CopyOnWriteArraySet[CachedStateListener]())
