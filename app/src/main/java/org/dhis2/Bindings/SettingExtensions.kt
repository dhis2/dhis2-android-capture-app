package org.dhis2.Bindings

import android.content.Context
import org.dhis2.R
import org.hisp.dhis.android.core.settings.DataSyncPeriod
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.MetadataSyncPeriod

const val EVERY_15_MIN = 15 * 60
const val EVERY_HOUR = 60 * 60
const val EVERY_6_HOUR = 6 * 60 * 60
const val EVERY_12_HOUR = 12 * 60 * 60
const val EVERY_24_HOUR = 24 * 60 * 60
const val EVERY_7_DAYS = 7 * 24 * 60 * 60

fun MetadataSyncPeriod.toSeconds(): Int {
    return when (this) {
        MetadataSyncPeriod.EVERY_HOUR -> 60 * 60
        MetadataSyncPeriod.EVERY_12_HOURS -> 12 * 60 * 60
        MetadataSyncPeriod.EVERY_DAY -> 24 * 60 * 60
        MetadataSyncPeriod.EVERY_7_DAYS -> 7 * 24 * 60 * 60
    }
}

fun DataSyncPeriod.toSeconds(): Int {
    return when (this) {
        DataSyncPeriod.EVERY_30_MIN -> 30 * 60
        DataSyncPeriod.EVERY_HOUR -> 60 * 60
        DataSyncPeriod.EVERY_6_HOURS -> 6 * 60 * 60
        DataSyncPeriod.EVERY_12_HOURS -> 12 * 60 * 60
        DataSyncPeriod.EVERY_24_HOURS -> 24 * 60 * 60
    }
}

fun LimitScope?.toTrailingText(context: Context):String{
    return when(this){
        LimitScope.ALL_ORG_UNITS -> context.getString(R.string.limit_scope_all_ou_trailing)
        LimitScope.PER_ORG_UNIT -> context.getString(R.string.limit_scope_ou_trailing)
        LimitScope.PER_PROGRAM -> context.getString(R.string.limit_scope_program_trailing)
        LimitScope.PER_OU_AND_PROGRAM -> context.getString(R.string.limit_scope_ou_program_trailing)
        else -> context.getString(R.string.limit_scope_global_trailing)
    }
}