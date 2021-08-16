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

fun RelativePeriod.isInDaily(): Boolean {
    return this == RelativePeriod.TODAY ||
        this == RelativePeriod.YESTERDAY ||
        this == RelativePeriod.LAST_3_DAYS ||
        this == RelativePeriod.LAST_7_DAYS ||
        this == RelativePeriod.LAST_14_DAYS ||
        this == RelativePeriod.LAST_30_DAYS ||
        this == RelativePeriod.LAST_60_DAYS ||
        this == RelativePeriod.LAST_90_DAYS ||
        this == RelativePeriod.LAST_180_DAYS
}

fun RelativePeriod.isInWeekly(): Boolean {
    return this == RelativePeriod.THIS_WEEK ||
        this == RelativePeriod.LAST_WEEK ||
        this == RelativePeriod.LAST_4_WEEKS ||
        this == RelativePeriod.LAST_12_WEEKS ||
        this == RelativePeriod.LAST_52_WEEKS
}

fun RelativePeriod.isInMonthly(): Boolean {
    return this == RelativePeriod.THIS_MONTH ||
        this == RelativePeriod.LAST_MONTH ||
        this == RelativePeriod.LAST_3_MONTHS ||
        this == RelativePeriod.LAST_6_MONTHS ||
        this == RelativePeriod.LAST_12_MONTHS ||
        this == RelativePeriod.MONTHS_THIS_YEAR
}

fun RelativePeriod.isInYearly(): Boolean {
    return this == RelativePeriod.THIS_YEAR ||
        this == RelativePeriod.LAST_YEAR ||
        this == RelativePeriod.LAST_5_YEARS ||
        this == RelativePeriod.LAST_10_YEARS
}

fun RelativePeriod.isInOther(): Boolean {
    return this == RelativePeriod.THIS_QUARTER ||
        this == RelativePeriod.LAST_QUARTER ||
        this == RelativePeriod.LAST_4_QUARTERS ||
        this == RelativePeriod.QUARTERS_THIS_YEAR
}

fun RelativePeriod.getThisFromPeriod(): RelativePeriod {
    return when {
        this.isInDaily() -> {
            RelativePeriod.TODAY
        }
        this.isInMonthly() -> {
            RelativePeriod.THIS_MONTH
        }
        this.isInWeekly() -> {
            RelativePeriod.THIS_WEEK
        }
        this.isInYearly() -> {
            RelativePeriod.THIS_YEAR
        }
        else -> {
            RelativePeriod.THIS_QUARTER
        }
    }
}
