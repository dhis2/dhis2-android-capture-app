package org.dhis2.mobile.aggregates

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.KStubbing
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T : Any, R> KStubbing<T>.onRunBlocking(noinline methodCall: suspend T.() -> R): OngoingStubbing<R> =
    on {
        runBlocking { methodCall() }
    }
