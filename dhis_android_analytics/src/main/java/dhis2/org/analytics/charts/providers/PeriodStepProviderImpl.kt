package dhis2.org.analytics.charts.providers

import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType

class PeriodStepProviderImpl(val d2: D2) : PeriodStepProvider {
    override fun periodStep(periodType: PeriodType?): Long {
        val currentDate = Date()
        val initialPeriodDate = getPeriodForPeriodTypeAndDate(
            periodType ?: PeriodType.Daily,
            currentDate,
            -1
        ).startDate()?.time ?: 0L
        val currentPeriodDate = getPeriodForPeriodTypeAndDate(
            periodType ?: PeriodType.Daily,
            currentDate,
            0
        ).startDate()?.time ?: 0L
        return currentPeriodDate - initialPeriodDate
    }

    private fun getPeriodForPeriodTypeAndDate(
        periodType: PeriodType,
        currentDate: Date,
        offset: Int
    ): Period {
        return d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
            periodType,
            currentDate,
            offset
        )
    }
}
