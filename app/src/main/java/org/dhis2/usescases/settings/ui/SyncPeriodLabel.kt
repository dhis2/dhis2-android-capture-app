package org.dhis2.usescases.settings.ui

import android.content.Context
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_7_DAYS
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants

internal fun syncPeriodLabel(
    currentDataSyncPeriod: Int,
    context: Context,
): String {
    val setting =
        when (currentDataSyncPeriod) {
            EVERY_30_MIN -> context.getString(R.string.thirty_minutes)
            EVERY_HOUR -> context.getString(R.string.a_hour)
            EVERY_6_HOUR -> context.getString(R.string.every_6_hours)
            EVERY_12_HOUR -> context.getString(R.string.every_12_hours)
            Constants.TIME_MANUAL -> context.getString(R.string.Manual)
            EVERY_24_HOUR -> context.getString(R.string.a_day)
            EVERY_7_DAYS -> context.getString(R.string.a_week)
            else -> context.getString(R.string.a_day)
        }

    return setting
}
