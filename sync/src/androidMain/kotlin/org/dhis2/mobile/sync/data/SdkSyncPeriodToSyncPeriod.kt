package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.model.SyncPeriod
import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod

internal fun MetadataSyncPeriod.toSyncPeriod() =
    when (this) {
        MetadataSyncPeriod.EVERY_HOUR -> SyncPeriod.EveryHour
        MetadataSyncPeriod.EVERY_6_HOURS -> SyncPeriod.Every6Hour
        MetadataSyncPeriod.EVERY_12_HOURS -> SyncPeriod.Every12Hour
        MetadataSyncPeriod.EVERY_24_HOURS -> SyncPeriod.Every24Hour
        MetadataSyncPeriod.EVERY_7_DAYS -> SyncPeriod.Every7Days
        MetadataSyncPeriod.MANUAL -> SyncPeriod.Manual
    }

internal fun DataSyncPeriod.toSyncPeriod() =
    when (this) {
        DataSyncPeriod.EVERY_30_MIN -> SyncPeriod.Every30Min
        DataSyncPeriod.EVERY_HOUR -> SyncPeriod.EveryHour
        DataSyncPeriod.EVERY_6_HOURS -> SyncPeriod.Every6Hour
        DataSyncPeriod.EVERY_12_HOURS -> SyncPeriod.Every12Hour
        DataSyncPeriod.EVERY_24_HOURS -> SyncPeriod.Every24Hour
        DataSyncPeriod.MANUAL -> SyncPeriod.Manual
    }
