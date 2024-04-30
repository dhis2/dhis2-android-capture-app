package dhis2.org.analytics.charts.providers

import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Locale

interface PeriodStepProvider {
    fun periodStep(periodType: PeriodType?): Long
    fun periodUIString(locale: Locale, period: Period): String

    fun getPeriodDiff(initialPeriod: Period, currentPeriod: Period): Int
}
