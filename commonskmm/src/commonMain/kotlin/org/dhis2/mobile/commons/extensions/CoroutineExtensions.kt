package org.dhis2.mobile.commons.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

suspend fun <T> CoroutineScope.withMinimumDuration(
    minimumDurationMillis: Long = 3000,
    block: suspend CoroutineScope.() -> T,
): T {
    val starTime = System.currentTimeMillis()
    val result = block()
    val duration = System.currentTimeMillis() - starTime
    if (duration < minimumDurationMillis) {
        delay(minimumDurationMillis - duration)
    }
    return result
}
