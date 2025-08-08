package org.dhis2.usescases.settings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_7_DAYS
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants

@Composable
internal fun syncPeriodLabel(currentDataSyncPeriod: Int): String {
    val setting =
        when (currentDataSyncPeriod) {
            EVERY_30_MIN -> stringResource(R.string.thirty_minutes)
            EVERY_HOUR -> stringResource(R.string.a_hour)
            EVERY_6_HOUR -> stringResource(R.string.every_6_hours)
            EVERY_12_HOUR -> stringResource(R.string.every_12_hours)
            Constants.TIME_MANUAL -> stringResource(R.string.Manual)
            EVERY_24_HOUR -> stringResource(R.string.a_day)
            EVERY_7_DAYS -> stringResource(R.string.a_week)
            else -> stringResource(R.string.a_day)
        }

    return setting
}
