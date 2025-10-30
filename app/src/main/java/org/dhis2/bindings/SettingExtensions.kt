package org.dhis2.bindings

import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod

const val EVERY_15_MIN = 15 * 60
const val EVERY_30_MIN = 30 * 60
const val EVERY_HOUR = 60 * 60
const val EVERY_6_HOUR = 6 * 60 * 60
const val EVERY_12_HOUR = 12 * 60 * 60
const val EVERY_24_HOUR = 24 * 60 * 60
const val EVERY_7_DAYS = 7 * 24 * 60 * 60
const val MANUAL = 0

fun MetadataSyncPeriod.toSeconds(): Int =
    when (this) {
        MetadataSyncPeriod.EVERY_HOUR -> EVERY_HOUR
        MetadataSyncPeriod.EVERY_12_HOURS -> EVERY_12_HOUR
        MetadataSyncPeriod.EVERY_24_HOURS -> EVERY_24_HOUR
        MetadataSyncPeriod.EVERY_7_DAYS -> EVERY_7_DAYS
        MetadataSyncPeriod.MANUAL -> MANUAL
    }

fun DataSyncPeriod.toSeconds(): Int =
    when (this) {
        DataSyncPeriod.EVERY_30_MIN -> EVERY_30_MIN
        DataSyncPeriod.EVERY_HOUR -> EVERY_HOUR
        DataSyncPeriod.EVERY_6_HOURS -> EVERY_6_HOUR
        DataSyncPeriod.EVERY_12_HOURS -> EVERY_12_HOUR
        DataSyncPeriod.EVERY_24_HOURS -> EVERY_24_HOUR
        DataSyncPeriod.MANUAL -> MANUAL
    }
