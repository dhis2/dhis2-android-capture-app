package dhis2.org.analytics.charts.extensions

import org.hisp.dhis.android.core.common.RelativePeriod

fun RelativePeriod.isNotCurrent(): Boolean {
    return this != RelativePeriod.TODAY &&
        this != RelativePeriod.THIS_WEEK &&
        this != RelativePeriod.THIS_MONTH &&
        this != RelativePeriod.THIS_YEAR &&
        this != RelativePeriod.THIS_QUARTER &&
        this != RelativePeriod.THIS_BIMONTH &&
        this != RelativePeriod.THIS_BIWEEK &&
        this != RelativePeriod.THIS_SIX_MONTH &&
        this != RelativePeriod.THIS_FINANCIAL_YEAR &&
        this != RelativePeriod.MONTHS_THIS_YEAR &&
        this != RelativePeriod.QUARTERS_THIS_YEAR
}