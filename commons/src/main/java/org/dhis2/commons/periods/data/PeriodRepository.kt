package org.dhis2.commons.periods.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class PeriodRepository(private val d2: D2) {

    fun generatePeriod(
        periodType: PeriodType,
        date: Date = Date(),
        offset: Int = 0,
    ) = d2.periodModule().periodHelper()
        .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)
}
