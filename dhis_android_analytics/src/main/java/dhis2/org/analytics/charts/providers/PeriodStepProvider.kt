package dhis2.org.analytics.charts.providers

import org.hisp.dhis.android.core.period.PeriodType

interface PeriodStepProvider {
    fun periodStep(periodType: PeriodType?): Long
}
