package org.dhis2.mobile.sync.model

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

sealed interface SyncPeriod {
    object Every15Min : SyncPeriod

    object Every30Min : SyncPeriod

    object EveryHour : SyncPeriod

    object Every6Hour : SyncPeriod

    object Every12Hour : SyncPeriod

    object Every24Hour : SyncPeriod

    object Every7Days : SyncPeriod

    object Manual : SyncPeriod

    fun toSeconds() =
        when (this) {
            Every12Hour -> 12.hours.inWholeSeconds
            Every15Min -> 15.minutes.inWholeSeconds
            Every24Hour -> 1.days.inWholeSeconds
            Every30Min -> 30.minutes.inWholeSeconds
            Every6Hour -> 6.hours.inWholeSeconds
            Every7Days -> 7.days.inWholeSeconds
            EveryHour -> 1.hours.inWholeSeconds
            Manual -> 0
        }
}

fun Long.toSyncPeriod() =
    when {
        this == 0.toLong() -> SyncPeriod.Manual
        this == 15.minutes.inWholeSeconds -> SyncPeriod.Every15Min
        this == 30.minutes.inWholeSeconds -> SyncPeriod.Every30Min
        this == 1.hours.inWholeSeconds -> SyncPeriod.EveryHour
        this == 6.hours.inWholeSeconds -> SyncPeriod.Every6Hour
        this == 12.hours.inWholeSeconds -> SyncPeriod.Every12Hour
        this == 1.days.inWholeSeconds -> SyncPeriod.Every24Hour
        this == 7.days.inWholeSeconds -> SyncPeriod.Every7Days
        else -> throw IllegalArgumentException("Invalid sync period")
    }
