package org.dhis2.mobile.commons.extensions

import java.util.Date
import kotlin.time.Instant

fun Date.toKtxInstant(): Instant = Instant.fromEpochMilliseconds(this.time)

fun Instant.toJavaDate(): Date = Date(this.toEpochMilliseconds())

fun getTodayAsInstant(): Instant {
    val nowMillis = System.currentTimeMillis()

    return Instant.fromEpochMilliseconds(nowMillis)
}
