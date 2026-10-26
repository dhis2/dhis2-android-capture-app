package org.dhis2.mobile.commons.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.time.TimeSource

suspend fun <T> CoroutineScope.withMinimumDuration(
    minimumDurationMillis: Long = 3000,
    block: suspend CoroutineScope.() -> T,
): T {
    val start = TimeSource.Monotonic.markNow()
    val result = block()
    val duration = start.elapsedNow().inWholeMilliseconds
    if (duration < minimumDurationMillis) {
        delay(minimumDurationMillis - duration)
    }
    return result
}
